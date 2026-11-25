package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import controle.CtrlFornecedor;
import dao.ConexaoSQLite;
import dominio.EntidadeDominio;
import dominio.Fornecedor;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainServer {
    private static final int PORTA = 8080;

    public static void main(String[] args) throws IOException {
        ConexaoSQLite.inicializarBancoSeNecessario("sql/schema.sql");
        CtrlFornecedor ctrl = new CtrlFornecedor();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORTA), 0);

        // Rota de domínios (RNF0013)
        server.createContext("/api/dominios", exchange -> {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                enviarJson(exchange, 405, JsonUtil.mensagemJson(false, "Método não permitido", null));
                return;
            }
            try {
                String json = JsonUtil.dominiosToJson(ctrl.consultarDominios());
                enviarJson(exchange, 200, json);
            } catch (Exception e) {
                enviarJson(exchange, 500, JsonUtil.mensagemJson(false, e.getMessage(), null));
            }
        });

        // Rotas de fornecedores (CDU01)
        server.createContext("/api/fornecedores", exchange -> {
            String method = exchange.getRequestMethod().toUpperCase();
            String path = exchange.getRequestURI().getPath();
            String resto = path.length() > "/api/fornecedores".length()
                    ? path.substring("/api/fornecedores".length()) : "";

            try {
                if ((resto.isEmpty() || "/".equals(resto)) && "GET".equals(method)) {
                    Map<String, String> filtros = parseQuery(exchange.getRequestURI().getRawQuery());
                    List<EntidadeDominio> lista = ctrl.consultar(filtros);
                    enviarJson(exchange, 200, JsonUtil.listaFornecedoresToJson(lista));

                } else if ((resto.isEmpty() || "/".equals(resto)) && "POST".equals(method)) {
                    Map<String, String> dados = JsonUtil.parseFlatObject(lerCorpo(exchange));
                    Fornecedor[] salvo = new Fornecedor[1];
                    String erro = ctrl.salvar(dados, salvo);
                    if (erro != null) {
                        enviarJson(exchange, 400, JsonUtil.mensagemJson(false, erro, null));
                    } else {
                        String msg = "Fornecedor cadastrado com sucesso! Código gerado: " + salvo[0].getCodigo();
                        enviarJson(exchange, 201, JsonUtil.mensagemJson(true, msg, salvo[0].getCodigo()));
                    }

                } else if (resto.matches("^/\\d+$") && "GET".equals(method)) {
                    Long id = Long.parseLong(resto.substring(1));
                    Fornecedor f = ctrl.consultarPorId(id);
                    if (f == null) {
                        enviarJson(exchange, 404, JsonUtil.mensagemJson(false, "Fornecedor não encontrado.", null));
                    } else {
                        enviarJson(exchange, 200, JsonUtil.fornecedorToJson(f));
                    }

                } else if (resto.matches("^/\\d+$") && "PUT".equals(method)) {
                    Long id = Long.parseLong(resto.substring(1));
                    Map<String, String> dados = JsonUtil.parseFlatObject(lerCorpo(exchange));
                    String erro = ctrl.alterar(id, dados);
                    if (erro != null) {
                        enviarJson(exchange, 400, JsonUtil.mensagemJson(false, erro, null));
                    } else {
                        enviarJson(exchange, 200, JsonUtil.mensagemJson(true, "Fornecedor alterado com sucesso!", null));
                    }

                } else if (resto.matches("^/\\d+/inativar$") && "POST".equals(method)) {
                    Long id = Long.parseLong(resto.split("/")[1]);
                    Map<String, String> dados = JsonUtil.parseFlatObject(lerCorpo(exchange));
                    String erro = ctrl.inativar(id, dados);
                    if (erro != null) {
                        enviarJson(exchange, 400, JsonUtil.mensagemJson(false, erro, null));
                    } else {
                        enviarJson(exchange, 200, JsonUtil.mensagemJson(true, "Fornecedor inativado com sucesso!", null));
                    }

                } else if (resto.matches("^/\\d+/ativar$") && "POST".equals(method)) {
                    Long id = Long.parseLong(resto.split("/")[1]);
                    Map<String, String> dados = JsonUtil.parseFlatObject(lerCorpo(exchange));
                    String erro = ctrl.ativar(id, dados);
                    if (erro != null) {
                        enviarJson(exchange, 400, JsonUtil.mensagemJson(false, erro, null));
                    } else {
                        enviarJson(exchange, 200, JsonUtil.mensagemJson(true, "Fornecedor ativado com sucesso!", null));
                    }

                } else {
                    enviarJson(exchange, 404, JsonUtil.mensagemJson(false, "Rota não encontrada.", null));
                }
            } catch (Exception e) {
                enviarJson(exchange, 500, JsonUtil.mensagemJson(false, "Erro interno: " + e.getMessage(), null));
            }
        });

        // Arquivos estáticos do Front-end (pasta web/)
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path)) path = "/index.html";
            Path arquivo = Path.of("web", path.substring(1)).normalize();
            if (!arquivo.startsWith(Path.of("web")) || !Files.exists(arquivo) || Files.isDirectory(arquivo)) {
                byte[] resp = "404 - Arquivo não encontrado".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(404, resp.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(resp); }
                return;
            }
            String contentType = "text/plain; charset=UTF-8";
            if (path.endsWith(".html")) contentType = "text/html; charset=UTF-8";
            else if (path.endsWith(".css")) contentType = "text/css; charset=UTF-8";
            else if (path.endsWith(".js")) contentType = "application/javascript; charset=UTF-8";

            byte[] bytes = Files.readAllBytes(arquivo);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Servidor rodando em http://localhost:" + PORTA);
    }

    private static String lerCorpo(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isBlank()) return map;
        for (String par : query.split("&")) {
            String[] kv = par.split("=", 2);
            String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String v = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            map.put(k, v);
        }
        return map;
    }

    private static void enviarJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
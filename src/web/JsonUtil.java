package web;

import dominio.*;
import java.util.*;

public class JsonUtil {

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // Parser simples para objeto JSON plano {"chave":"valor", "num":123}
    public static Map<String, String> parseFlatObject(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null) return map;
        String s = json.trim();
        if (s.startsWith("{")) s = s.substring(1);
        if (s.endsWith("}")) s = s.substring(0, s.length() - 1);

        int i = 0;
        int n = s.length();
        while (i < n) {
            while (i < n && (Character.isWhitespace(s.charAt(i)) || s.charAt(i) == ',')) i++;
            if (i >= n || s.charAt(i) != '"') break;
            int[] next = new int[1];
            String key = readString(s, i, next);
            i = next[0];
            while (i < n && s.charAt(i) != ':') i++;
            i++; // pula ':'
            while (i < n && Character.isWhitespace(s.charAt(i))) i++;
            if (i >= n) break;

            String val;
            if (s.charAt(i) == '"') {
                val = readString(s, i, next);
                i = next[0];
            } else {
                int start = i;
                while (i < n && s.charAt(i) != ',') i++;
                val = s.substring(start, i).trim();
                if ("null".equals(val)) val = null;
            }
            map.put(key, val);
        }
        return map;
    }

    private static String readString(String s, int start, int[] nextPos) {
        StringBuilder sb = new StringBuilder();
        int i = start + 1;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char nxt = s.charAt(i + 1);
                if (nxt == 'n') sb.append('\n');
                else if (nxt == 'r') sb.append('\r');
                else if (nxt == 't') sb.append('\t');
                else sb.append(nxt);
                i += 2;
            } else if (c == '"') {
                nextPos[0] = i + 1;
                return sb.toString();
            } else {
                sb.append(c);
                i++;
            }
        }
        nextPos[0] = i;
        return sb.toString();
    }

    public static String mensagemJson(boolean sucesso, String mensagem, String codigo) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"sucesso\":").append(sucesso).append(",");
        sb.append("\"mensagem\":\"").append(escape(mensagem)).append("\"");
        if (codigo != null) {
            sb.append(",\"codigo\":\"").append(escape(codigo)).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    public static String fornecedorToJson(Fornecedor f) {
        Telefone t = f.getTelefone();
        Endereco e = f.getEndereco();
        Estado est = e.getEstado();
        Pais p = est.getPais();

        return "{"
            + "\"id\":" + f.getId() + ","
            + "\"codigo\":\"" + escape(f.getCodigo()) + "\","
            + "\"razaoSocial\":\"" + escape(f.getRazaoSocial()) + "\","
            + "\"nomeFantasia\":\"" + escape(f.getNomeFantasia()) + "\","
            + "\"cnpj\":\"" + escape(f.getCnpj()) + "\","
            + "\"email\":\"" + escape(f.getEmail()) + "\","
            + "\"status\":\"" + f.getStatus().name() + "\","
            + "\"statusDescricao\":\"" + escape(f.getStatus().getDescricao()) + "\","
            + "\"telefone\":{"
                + "\"id\":" + t.getId() + ","
                + "\"tipoId\":" + t.getTipo().getId() + ","
                + "\"tipoDescricao\":\"" + escape(t.getTipo().getDescricao()) + "\","
                + "\"ddd\":\"" + escape(t.getDdd()) + "\","
                + "\"numero\":\"" + escape(t.getNumero()) + "\""
            + "},"
            + "\"endereco\":{"
                + "\"id\":" + e.getId() + ","
                + "\"tipoLogradouroId\":" + e.getTipoLogradouro().getId() + ","
                + "\"tipoLogradouroDescricao\":\"" + escape(e.getTipoLogradouro().getDescricao()) + "\","
                + "\"logradouro\":\"" + escape(e.getLogradouro()) + "\","
                + "\"numero\":\"" + escape(e.getNumero()) + "\","
                + "\"complemento\":\"" + escape(e.getComplemento() == null ? "" : e.getComplemento()) + "\","
                + "\"bairro\":\"" + escape(e.getBairro()) + "\","
                + "\"cep\":\"" + escape(e.getCep()) + "\","
                + "\"cidade\":\"" + escape(e.getCidade()) + "\","
                + "\"estadoId\":" + est.getId() + ","
                + "\"estadoUf\":\"" + escape(est.getUf()) + "\","
                + "\"estadoNome\":\"" + escape(est.getNome()) + "\","
                + "\"paisId\":" + p.getId() + ","
                + "\"paisNome\":\"" + escape(p.getNome()) + "\""
            + "}"
            + "}";
    }

    public static String listaFornecedoresToJson(List<EntidadeDominio> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(fornecedorToJson((Fornecedor) lista.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    public static String dominiosToJson(Map<String, List<EntidadeDominio>> dom) {
        StringBuilder sb = new StringBuilder("{");
        int k = 0;
        for (Map.Entry<String, List<EntidadeDominio>> entry : dom.entrySet()) {
            if (k++ > 0) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":[");
            List<EntidadeDominio> itens = entry.getValue();
            for (int i = 0; i < itens.size(); i++) {
                if (i > 0) sb.append(",");
                EntidadeDominio item = itens.get(i);
                if (item instanceof Pais) {
                    Pais p = (Pais) item;
                    sb.append("{\"id\":").append(p.getId()).append(",\"nome\":\"").append(escape(p.getNome()))
                      .append("\",\"sigla\":\"").append(escape(p.getSigla())).append("\"}");
                } else if (item instanceof Estado) {
                    Estado e = (Estado) item;
                    sb.append("{\"id\":").append(e.getId()).append(",\"nome\":\"").append(escape(e.getNome()))
                      .append("\",\"uf\":\"").append(escape(e.getUf())).append("\",\"paisId\":").append(e.getPais().getId()).append("}");
                } else if (item instanceof TipoLogradouro) {
                    TipoLogradouro t = (TipoLogradouro) item;
                    sb.append("{\"id\":").append(t.getId()).append(",\"descricao\":\"").append(escape(t.getDescricao())).append("\"}");
                } else if (item instanceof TipoTelefone) {
                    TipoTelefone t = (TipoTelefone) item;
                    sb.append("{\"id\":").append(t.getId()).append(",\"descricao\":\"").append(escape(t.getDescricao())).append("\"}");
                } else if (item instanceof CategoriaInativacao) {
                    CategoriaInativacao c = (CategoriaInativacao) item;
                    sb.append("{\"id\":").append(c.getId()).append(",\"descricao\":\"").append(escape(c.getDescricao())).append("\"}");
                } else if (item instanceof CategoriaAtivacao) {
                    CategoriaAtivacao c = (CategoriaAtivacao) item;
                    sb.append("{\"id\":").append(c.getId()).append(",\"descricao\":\"").append(escape(c.getDescricao())).append("\"}");
                }
            }
            sb.append("]");
        }
        sb.append("}");
        return sb.toString();
    }
}
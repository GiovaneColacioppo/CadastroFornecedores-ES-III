const USUARIO_ATUAL = 'Carlos Mendes / Administrador';

let dominios = {
    paises: [],
    estados: [],
    tiposLogradouro: [],
    tiposTelefone: [],
    categoriasInativacao: [],
    categoriasAtivacao: []
};

let fornecedoresEncontrados = [];
let fornecedorSelecionado = null;
let modoCadastro = 'novo'; // 'novo' | 'alterar' | 'visualizar'
let acaoStatusAtual = '';  // 'inativar' | 'ativar'

// Telas
const telaBusca = document.getElementById('tela-busca');
const telaResultado = document.getElementById('tela-resultado');
const telaCadastro = document.getElementById('tela-cadastro');
const telaStatus = document.getElementById('tela-status');

// Botões
const btnNovo = document.getElementById('btn-novo');
const btnVisualizar = document.getElementById('btn-visualizar');
const btnAlterar = document.getElementById('btn-alterar');
const btnInativar = document.getElementById('btn-inativar');
const btnAtivar = document.getElementById('btn-ativar');

// Alertas
const alertaGlobal = document.getElementById('alerta-global');
const alertaCadastro = document.getElementById('alerta-cadastro');
const alertaStatus = document.getElementById('alerta-status');
const mensagemVazia = document.getElementById('mensagem-vazia');

// ==============================================
// Inicialização e Domínios (RNF0013)
// ==============================================
document.addEventListener('DOMContentLoaded', async () => {
    document.getElementById('usuario-autenticado').textContent = USUARIO_ATUAL;
    await carregarDominios();
});

async function carregarDominios() {
    try {
        const resp = await fetch('/api/dominios');
        if (!resp.ok) throw new Error('Falha ao carregar tabelas de domínio.');
        dominios = await resp.json();

        preencherSelect('b-estado', dominios.estados, e => e.id, e => `${e.uf} - ${e.nome}`, 'Todos');
        preencherSelect('c-end-estado', dominios.estados, e => e.id, e => `${e.uf} - ${e.nome}`, 'Selecione');
        preencherSelect('c-end-pais', dominios.paises, p => p.id, p => p.nome, 'Selecione');
        preencherSelect('c-end-tipo', dominios.tiposLogradouro, t => t.id, t => t.descricao, 'Selecione');
        preencherSelect('c-tel-tipo', dominios.tiposTelefone, t => t.id, t => t.descricao, 'Selecione');
    } catch (err) {
        exibirAlertaGlobal(err.message, 'danger');
    }
}

function preencherSelect(idSelect, itens, getValue, getLabel, textoPadrao) {
    const select = document.getElementById(idSelect);
    select.innerHTML = `<option value="">${textoPadrao}</option>`;
    itens.forEach(item => {
        const opt = document.createElement('option');
        opt.value = getValue(item);
        opt.textContent = getLabel(item);
        select.appendChild(opt);
    });
}

// Vincula país automaticamente ao escolher estado
document.getElementById('c-end-estado').addEventListener('change', (e) => {
    const estadoId = Number(e.target.value);
    const est = dominios.estados.find(item => item.id === estadoId);
    if (est) {
        document.getElementById('c-end-pais').value = String(est.paisId);
    }
});

// ==============================================
// Utilitários de Interface
// ==============================================
function alternarTela(telaVisivel, mostrarResultados = false) {
    document.querySelectorAll('.tela').forEach(t => t.classList.remove('ativo'));
    telaVisivel.classList.add('ativo');
    if (mostrarResultados) {
        telaResultado.classList.add('ativo');
    }
}

function exibirAlertaGlobal(mensagem, tipo = 'success') {
    alertaGlobal.textContent = mensagem;
    alertaGlobal.className = `alert alert-${tipo}`;
}

function limparAlertaGlobal() {
    alertaGlobal.className = 'alert d-none';
    alertaGlobal.textContent = '';
}

function formatarCnpj(valor) {
    const digitos = (valor || '').replace(/\D/g, '');
    if (digitos.length !== 14) return valor || '';
    return digitos.replace(/^(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})$/, '$1.$2.$3/$4-$5');
}

function escapeHtml(texto) {
    return String(texto ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

// ==============================================
// Tela de Busca (11.1) e Resultado (11.2)
// ==============================================

// P1.3: Alterar qualquer parâmetro de busca desabilita o botão "Novo" até nova consulta
document.querySelectorAll('#form-busca input, #form-busca select').forEach(campo => {
    const desabilitarNovo = () => { btnNovo.disabled = true; };
    campo.addEventListener('input', desabilitarNovo);
    campo.addEventListener('change', desabilitarNovo);
});

document.getElementById('form-busca').addEventListener('submit', async (e) => {
    e.preventDefault();
    limparAlertaGlobal();
    await executarBusca();
});

async function executarBusca() {
    const params = new URLSearchParams();
    const codigo = document.getElementById('b-codigo').value.trim();
    const razaoSocial = document.getElementById('b-razao-social').value.trim();
    const nomeFantasia = document.getElementById('b-nome-fantasia').value.trim();
    const cnpj = document.getElementById('b-cnpj').value.trim();
    const cidade = document.getElementById('b-cidade').value.trim();
    const estadoId = document.getElementById('b-estado').value;
    const status = document.getElementById('b-status').value;

    if (codigo) params.set('codigo', codigo);
    if (razaoSocial) params.set('razaoSocial', razaoSocial);
    if (nomeFantasia) params.set('nomeFantasia', nomeFantasia);
    if (cnpj) params.set('cnpj', cnpj);
    if (cidade) params.set('cidade', cidade);
    if (estadoId) params.set('estadoId', estadoId);
    if (status) params.set('status', status);

    try {
        const resp = await fetch(`/api/fornecedores?${params.toString()}`);
        if (!resp.ok) throw new Error('Erro ao consultar fornecedores.');
        fornecedoresEncontrados = await resp.json();

        renderizarTabela(fornecedoresEncontrados);
        alternarTela(telaBusca, true);
        btnNovo.disabled = false; // P1.3 e E3: habilita "Novo" após a consulta
        limparSelecao();
    } catch (err) {
        exibirAlertaGlobal(err.message, 'danger');
    }
}

function renderizarTabela(lista) {
    const tbody = document.getElementById('tabela-resultados');
    tbody.innerHTML = '';

    if (lista.length === 0) {
        mensagemVazia.classList.remove('d-none'); // E3: Nenhum fornecedor encontrado
        return;
    }
    mensagemVazia.classList.add('d-none');

    lista.forEach(f => {
        const tr = document.createElement('tr');
        const badgeClass = f.status === 'ATIVO' ? 'bg-success' : 'bg-danger';

        tr.innerHTML = `
            <td class="text-center">
                <input class="form-check-input" type="radio" name="selecao-fornecedor" value="${f.id}">
            </td>
            <td>${escapeHtml(f.codigo)}</td>
            <td>${escapeHtml(f.razaoSocial)}</td>
            <td>${escapeHtml(f.nomeFantasia)}</td>
            <td>${escapeHtml(formatarCnpj(f.cnpj))}</td>
            <td>${escapeHtml(f.endereco.cidade)}/${escapeHtml(f.endereco.estadoUf)}</td>
            <td><span class="badge ${badgeClass}">${escapeHtml(f.statusDescricao)}</span></td>
        `;

        tr.addEventListener('click', () => {
            const radio = tr.querySelector('input[type="radio"]');
            radio.checked = true;
            selecionarFornecedor(f.id);
        });

        tbody.appendChild(tr);
    });
}

function selecionarFornecedor(id) {
    fornecedorSelecionado = fornecedoresEncontrados.find(f => f.id === id) || null;
    if (!fornecedorSelecionado) {
        limparSelecao();
        return;
    }

    // P2: Habilitação condicional conforme seleção e status
    btnVisualizar.disabled = false;
    btnAlterar.disabled = false;
    btnInativar.disabled = fornecedorSelecionado.status !== 'ATIVO';
    btnAtivar.disabled = fornecedorSelecionado.status !== 'INATIVO';
}

function limparSelecao() {
    fornecedorSelecionado = null;
    document.getElementsByName('selecao-fornecedor').forEach(r => { r.checked = false; });
    btnVisualizar.disabled = true;
    btnAlterar.disabled = true;
    btnInativar.disabled = true;
    btnAtivar.disabled = true;
}

// A2.1: Consulta pontual do fornecedor selecionado
async function consultarFornecedorPontual(id) {
    const resp = await fetch(`/api/fornecedores/${id}`);
    if (!resp.ok) {
        const err = await resp.json().catch(() => ({}));
        throw new Error(err.mensagem || 'Fornecedor não encontrado.');
    }
    return await resp.json();
}

// ==============================================
// Tela de Cadastro / Alteração / Visualização (11.3)
// ==============================================
btnNovo.addEventListener('click', () => {
    limparAlertaGlobal();
    prepararFormularioCadastro('novo');
    // P1.3 / P4.1: Recupera CNPJ da busca, se informado
    const cnpjBusca = document.getElementById('b-cnpj').value.trim();
    if (cnpjBusca) {
        document.getElementById('c-cnpj').value = cnpjBusca;
    }
    alternarTela(telaCadastro);
});

btnVisualizar.addEventListener('click', async () => {
    if (!fornecedorSelecionado) return;
    try {
        limparAlertaGlobal();
        const dados = await consultarFornecedorPontual(fornecedorSelecionado.id);
        prepararFormularioCadastro('visualizar', dados);
        alternarTela(telaCadastro);
    } catch (err) {
        exibirAlertaGlobal(err.message, 'danger');
    }
});

btnAlterar.addEventListener('click', async () => {
    if (!fornecedorSelecionado) return;
    try {
        limparAlertaGlobal();
        const dados = await consultarFornecedorPontual(fornecedorSelecionado.id);
        prepararFormularioCadastro('alterar', dados);
        alternarTela(telaCadastro);
    } catch (err) {
        exibirAlertaGlobal(err.message, 'danger');
    }
});

function prepararFormularioCadastro(modo, dados = null) {
    modoCadastro = modo;
    const form = document.getElementById('form-cadastro');
    form.reset();
    alertaCadastro.classList.add('d-none');

    const titulo = document.getElementById('titulo-cadastro');
    const btnSalvar = document.getElementById('btn-salvar');
    const btnCancelar = document.getElementById('btn-cancelar-cadastro');
    const grupoStatus = document.getElementById('grupo-status-visualizacao');
    const campos = form.querySelectorAll('input, select');

    campos.forEach(c => {
        c.removeAttribute('disabled');
        c.removeAttribute('readonly');
    });
    document.getElementById('c-codigo').setAttribute('readonly', 'true');
    btnSalvar.style.display = 'inline-block';
    btnCancelar.textContent = 'Cancelar';
    grupoStatus.classList.add('d-none');

    if (modo === 'novo') {
        titulo.innerHTML = '<i class="bi bi-building-add me-2"></i>Novo Fornecedor';
        if (dominios.paises.length === 1) {
            document.getElementById('c-end-pais').value = String(dominios.paises[0].id);
        }
        return;
    }

    if (dados) {
        fornecedorSelecionado = dados;
        document.getElementById('c-codigo').value = dados.codigo;
        document.getElementById('c-cnpj').value = formatarCnpj(dados.cnpj);
        document.getElementById('c-razao-social').value = dados.razaoSocial;
        document.getElementById('c-nome-fantasia').value = dados.nomeFantasia;
        document.getElementById('c-email').value = dados.email;
        document.getElementById('c-status').value = dados.statusDescricao;

        document.getElementById('c-tel-tipo').value = String(dados.telefone.tipoId);
        document.getElementById('c-tel-ddd').value = dados.telefone.ddd;
        document.getElementById('c-tel-numero').value = dados.telefone.numero;

        document.getElementById('c-end-tipo').value = String(dados.endereco.tipoLogradouroId);
        document.getElementById('c-end-logradouro').value = dados.endereco.logradouro;
        document.getElementById('c-end-numero').value = dados.endereco.numero;
        document.getElementById('c-end-complemento').value = dados.endereco.complemento || '';
        document.getElementById('c-end-bairro').value = dados.endereco.bairro;
        document.getElementById('c-end-cep').value = dados.endereco.cep;
        document.getElementById('c-end-cidade').value = dados.endereco.cidade;
        document.getElementById('c-end-estado').value = String(dados.endereco.estadoId);
        document.getElementById('c-end-pais').value = String(dados.endereco.paisId);

        if (modo === 'alterar') {
            titulo.innerHTML = '<i class="bi bi-building-gear me-2"></i>Alterar Fornecedor';
        } else if (modo === 'visualizar') {
            titulo.innerHTML = '<i class="bi bi-building-check me-2"></i>Visualizar Fornecedor';
            grupoStatus.classList.remove('d-none'); // A1.1 exibe status
            campos.forEach(c => c.setAttribute('disabled', 'true'));
            btnSalvar.style.display = 'none';
            btnCancelar.textContent = 'Voltar';
        }
    }
}

document.getElementById('form-cadastro').addEventListener('submit', async (e) => {
    e.preventDefault();
    alertaCadastro.classList.add('d-none');

    const payload = {
        codigo: document.getElementById('c-codigo').value.trim(),
        cnpj: document.getElementById('c-cnpj').value.trim(),
        razaoSocial: document.getElementById('c-razao-social').value.trim(),
        nomeFantasia: document.getElementById('c-nome-fantasia').value.trim(),
        email: document.getElementById('c-email').value.trim(),
        tipoTelefoneId: document.getElementById('c-tel-tipo').value,
        ddd: document.getElementById('c-tel-ddd').value.trim(),
        numeroTelefone: document.getElementById('c-tel-numero').value.trim(),
        tipoLogradouroId: document.getElementById('c-end-tipo').value,
        logradouro: document.getElementById('c-end-logradouro').value.trim(),
        numero: document.getElementById('c-end-numero').value.trim(),
        complemento: document.getElementById('c-end-complemento').value.trim(),
        bairro: document.getElementById('c-end-bairro').value.trim(),
        cep: document.getElementById('c-end-cep').value.trim(),
        cidade: document.getElementById('c-end-cidade').value.trim(),
        estadoId: document.getElementById('c-end-estado').value,
        paisId: document.getElementById('c-end-pais').value,
        usuario: USUARIO_ATUAL
    };

    const url = modoCadastro === 'alterar'
        ? `/api/fornecedores/${fornecedorSelecionado.id}`
        : '/api/fornecedores';
    const method = modoCadastro === 'alterar' ? 'PUT' : 'POST';

    try {
        const resp = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const resultado = await resp.json();

        if (!resp.ok || !resultado.sucesso) {
            // E1 / E2: mantém os campos preenchidos e exibe os erros retornados pelas Strategies
            alertaCadastro.textContent = resultado.mensagem || 'Erro ao salvar fornecedor.';
            alertaCadastro.classList.remove('d-none');
            return;
        }

        // P8.1 e P8.4: exibe código gerado/sucesso e volta a P1.2
        await executarBusca();
        exibirAlertaGlobal(resultado.mensagem, 'success');
    } catch (err) {
        alertaCadastro.textContent = 'Erro de comunicação com o servidor.';
        alertaCadastro.classList.remove('d-none');
    }
});

// ==============================================
// Tela de Inativação e Ativação (11.4)
// ==============================================
btnInativar.addEventListener('click', async () => {
    if (!fornecedorSelecionado) return;
    try {
        limparAlertaGlobal();
        const dados = await consultarFornecedorPontual(fornecedorSelecionado.id);
        prepararTelaStatus('inativar', dados);
        alternarTela(telaStatus);
    } catch (err) {
        exibirAlertaGlobal(err.message, 'danger');
    }
});

btnAtivar.addEventListener('click', async () => {
    if (!fornecedorSelecionado) return;
    try {
        limparAlertaGlobal();
        const dados = await consultarFornecedorPontual(fornecedorSelecionado.id);
        prepararTelaStatus('ativar', dados);
        alternarTela(telaStatus);
    } catch (err) {
        exibirAlertaGlobal(err.message, 'danger');
    }
});

function prepararTelaStatus(acao, dados) {
    acaoStatusAtual = acao;
    fornecedorSelecionado = dados;
    document.getElementById('form-status').reset();
    alertaStatus.classList.add('d-none');

    document.getElementById('info-fornecedor-status').textContent =
        `Fornecedor: ${dados.codigo} — ${dados.razaoSocial}`;

    if (acao === 'inativar') {
        document.getElementById('titulo-status').innerHTML =
            '<i class="bi bi-x-octagon me-2"></i>Inativar Fornecedor';
        preencherSelect('s-categoria', dominios.categoriasInativacao, c => c.id, c => c.descricao, 'Selecione');
    } else {
        document.getElementById('titulo-status').innerHTML =
            '<i class="bi bi-check-circle me-2"></i>Ativar Fornecedor';
        preencherSelect('s-categoria', dominios.categoriasAtivacao, c => c.id, c => c.descricao, 'Selecione');
    }
}

document.getElementById('form-status').addEventListener('submit', async (e) => {
    e.preventDefault();
    alertaStatus.classList.add('d-none');

    const payload = {
        categoriaId: document.getElementById('s-categoria').value,
        justificativa: document.getElementById('s-justificativa').value.trim(),
        usuario: USUARIO_ATUAL
    };

    try {
        const resp = await fetch(`/api/fornecedores/${fornecedorSelecionado.id}/${acaoStatusAtual}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const resultado = await resp.json();

        if (!resp.ok || !resultado.sucesso) {
            // E4: exibe campo não informado mantendo o texto digitado
            alertaStatus.textContent = resultado.mensagem || 'Erro ao alterar status.';
            alertaStatus.classList.remove('d-none');
            return;
        }

        // A4.5 / A5.5: exibe sucesso e volta a P1.2
        await executarBusca();
        exibirAlertaGlobal(resultado.mensagem, 'success');
    } catch (err) {
        alertaStatus.textContent = 'Erro de comunicação com o servidor.';
        alertaStatus.classList.remove('d-none');
    }
});

// ==============================================
// Botões Cancelar / Voltar (retorna a P1.2)
// ==============================================
document.querySelectorAll('.btn-cancelar').forEach(btn => {
    btn.addEventListener('click', async () => {
        await executarBusca();
    });
});
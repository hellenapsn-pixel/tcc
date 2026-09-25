// =========================
// MENSAGEM
// =========================

function mostrarMensagem() {
    alert("Bem-vindo ao GameBoxd!");
}


// =========================
// BUSCAR JOGO
// =========================

function buscarJogo() {

    let campo = document.getElementById("campoBusca");
    let resultado = document.getElementById("resultadoBusca");

    if (!campo || !resultado) {
        return;
    }

    let nome = campo.value.toLowerCase().trim();

    if (nome === "") {
        resultado.innerHTML = "<p>Digite o nome de um jogo.</p>";
        return;
    }

    if (nome.includes("resident evil 4")) {

        resultado.innerHTML = `
            <div class="jogo-destaque">

                <div class="capa-jogo">
                    <img
                        src="https://images.igdb.com/igdb/image/upload/t_cover_big/co1r7f.jpg"
                        alt="Resident Evil 4"
                    >
                </div>

                <div class="informacoes-jogo">

                    <h2>Resident Evil 4</h2>

                    <p class="nota">⭐ 5.0</p>

                    <p>
                        <strong>Gênero:</strong>
                        Terror / Ação
                    </p>

                    <p>
                        <strong>Plataforma:</strong>
                        PlayStation, Xbox e PC
                    </p>

                    <p>
                        <strong>Ano:</strong>
                        2023
                    </p>

                    <p>
                        Leon S. Kennedy é enviado para
                        resgatar a filha do presidente em
                        uma missão cheia de perigos.
                    </p>

                    <button onclick="adicionarBiblioteca()">
                        + Minha biblioteca
                    </button>

                </div>

            </div>
        `;

    } else {

        resultado.innerHTML = `
            <p>Nenhum jogo encontrado.</p>
        `;
    }
}


// =========================
// ADICIONAR À BIBLIOTECA
// =========================

function adicionarBiblioteca() {

    alert("Jogo adicionado à biblioteca!");
}


// =========================
// LOGIN
// =========================

function fazerLogin() {

    let email = document.getElementById("email");
    let senha = document.getElementById("senha");
    let mensagem = document.getElementById("mensagemLogin");

    if (!email || !senha || !mensagem) {
        return;
    }

    let emailValor = email.value.trim();
    let senhaValor = senha.value;

    if (emailValor === "" || senhaValor === "") {

        mensagem.innerText =
            "Preencha todos os campos.";

        return;
    }

    /*
     * LOGIN TEMPORÁRIO
     *
     * Depois vamos ligar o login
     * ao banco SQLite.
     */

    if (
        emailValor === "helena@email.com" &&
        senhaValor === "123456"
    ) {

        mensagem.innerText =
            "Login realizado com sucesso!";

        setTimeout(function () {

            window.location.href = "index.html";

        }, 1000);

    } else {

        mensagem.innerText =
            "E-mail ou senha incorretos.";
    }
}

// =========================
// CADASTRO
// =========================

function cadastrarUsuario() {

    let nome = document.getElementById("nome");
    let username = document.getElementById("username");
    let email = document.getElementById("email");
    let senha = document.getElementById("senha");
    let pais = document.getElementById("pais");
    let plataforma = document.getElementById("plataforma");
    let bio = document.getElementById("bio");
    let foto = document.getElementById("foto");
    let mensagem = document.getElementById("mensagemCadastro");

    if (
        !nome ||
        !username ||
        !email ||
        !senha ||
        !dataNascimento ||
        !pais ||
        !plataforma ||
        !bio ||
        !foto ||
        !mensagem
    ) {
        return;
    }

    if (
        nome.value.trim() === "" ||
        username.value.trim() === "" ||
        email.value.trim() === "" ||
        senha.value === "" ||
        dataNascimento.value === "" ||
        pais.value.trim() === "" ||
        plataforma.value === ""
    ) {

        mensagem.innerText =
            "Preencha os campos obrigatórios.";

        return;
    }

    mensagem.innerText =
        "Cadastro realizado com sucesso!";

    console.log("======================");
    console.log("USUÁRIO CADASTRADO");
    console.log("======================");

    console.log("Nome:", nome.value);
    console.log("Usuário:", username.value);
    console.log("E-mail:", email.value);
    console.log("Senha:", senha.value);
    console.log("País:", pais.value);
    console.log("Plataforma:", plataforma.value);
    console.log("Bio:", bio.value);
    console.log("Foto:", foto.value);

    setTimeout(function () {

        window.location.href = "login.html";

    }, 1500);
}
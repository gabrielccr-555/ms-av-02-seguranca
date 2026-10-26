package aula.microservicos.seguranca;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.hamcrest.Matchers;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureObservability
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SegurancaApplicationTests {

	@Autowired
	private MockMvc mvc;

	private static String json(String a, String b, String va, String vb) {
		return "{\"" + a + "\":\"" + va + "\",\"" + b + "\":\"" + vb + "\"}";
	}

	private void login(String user, String senha, int esperado) throws Exception {
		mvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON)
				.content(json("username", "senha", user, senha)))
				.andExpect(status().is(esperado));
	}

	@Test
	void loginValidoEInvalido() throws Exception {
		login("joao", "123456", 200);
		login("joao", "errada", 401);
		login("inexistente", "x", 401);
	}

	@Test
	void bloqueiaNaSegundaFalhaEDesbloqueiaComAdmin() throws Exception {
		login("maria", "errada", 401);
		login("maria", "abc123", 200);   // sucesso zera o contador
		login("maria", "errada", 401);
		login("maria", "errada", 401);   // 2a falha consecutiva -> bloqueia
		login("maria", "abc123", 401);   // bloqueado mesmo com senha correta

		// manutencao nao pode desbloquear
		mvc.perform(put("/unlock/maria").with(httpBasic("manutencao", "manutencao")))
				.andExpect(status().isForbidden());
		// sem credenciais
		mvc.perform(put("/unlock/maria")).andExpect(status().isUnauthorized());

		mvc.perform(put("/unlock/maria").with(httpBasic("admin", "admin")))
				.andExpect(status().isOk());
		login("maria", "abc123", 200);
	}

	@Test
	void trocaSenha() throws Exception {
		mvc.perform(put("/trocarsenha/joao").contentType(MediaType.APPLICATION_JSON)
				.content(json("atual", "nova", "errada", "nova1")))
				.andExpect(status().isUnauthorized());
		mvc.perform(put("/trocarsenha/joao").contentType(MediaType.APPLICATION_JSON)
				.content(json("atual", "nova", "123456", "nova1")))
				.andExpect(status().isOk());
		login("joao", "nova1", 200);
		mvc.perform(put("/trocarsenha/ninguem").contentType(MediaType.APPLICATION_JSON)
				.content(json("atual", "nova", "a", "b")))
				.andExpect(status().isNotFound());
	}

	@Test
	void cadastroEListagemComRoleManutencao() throws Exception {
		String novo = json("username", "senha", "pedro", "senha1");

		mvc.perform(post("/").contentType(MediaType.APPLICATION_JSON).content(novo))
				.andExpect(status().isUnauthorized());
		mvc.perform(post("/").with(httpBasic("admin", "admin")).contentType(MediaType.APPLICATION_JSON).content(novo))
				.andExpect(status().isForbidden());
		mvc.perform(post("/").with(httpBasic("manutencao", "manutencao")).contentType(MediaType.APPLICATION_JSON).content(novo))
				.andExpect(status().isCreated());
		mvc.perform(post("/").with(httpBasic("manutencao", "manutencao")).contentType(MediaType.APPLICATION_JSON).content(novo))
				.andExpect(status().isConflict());

		login("pedro", "senha1", 200);

		mvc.perform(get("/").with(httpBasic("admin", "admin"))).andExpect(status().isForbidden());
		mvc.perform(get("/").with(httpBasic("manutencao", "manutencao")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(3))
				.andExpect(jsonPath("$[0].senha").doesNotExist());
	}

	@Test
	void metricasEDocumentacao() throws Exception {
		login("joao", "123456", 200);
		login("maria", "x", 401);
		login("maria", "x", 401);

		mvc.perform(get("/actuator/prometheus"))
				.andExpect(status().isOk())
				.andExpect(content().string(Matchers.containsString("login_validos_total")))
				.andExpect(content().string(Matchers.containsString("login_invalidos_total")))
				.andExpect(content().string(Matchers.matchesRegex("(?s).*usuarios_bloqueados\\{[^}]*\\} 1\\.0.*")));

		mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
	}
}

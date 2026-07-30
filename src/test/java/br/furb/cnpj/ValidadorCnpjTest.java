package br.furb.cnpj;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Requisitos testáveis do Validador de CNPJ escritos em Gherkin e traduzidos
 * para JUnit 5.
 *
 * <p>
 * Fase RED do TDD: {@link ValidadorCnpj} é apenas um esqueleto (stub), logo
 * todos os cenários abaixo devem falhar. O código, porém, compila normalmente.
 *
 * <p>
 * DECISÕES DE INTERPRETAÇÃO (documentadas conforme o item "Resolução de
 * Conflitos" da atividade; os cenários recebidos não especificam estes pontos):
 *
 * <ul>
 * <li>ASSINATURA: o Gherkin exige tanto um resultado booleano ("retornar o
 * resultado verdadeiro/falso") quanto uma mensagem ("deve informar a
 * mensagem X"). Assumi um objeto de retorno {@code ResultadoValidacao} com
 * {@code valido()} e {@code mensagem()}, em vez de um {@code boolean} com
 * estado interno, para manter o validador sem efeito colateral entre
 * chamadas.</li>
 *
 * <li>MÁSCARA: os cenários 1, 2 e 5 informam o CNPJ pontuado e o cenário 3 o
 * informa sem pontuação. Assumi que o validador aceita ambas as formas e que
 * a máscara ({@code . / -}) é descartada antes de qualquer verificação.</li>
 *
 * <li>PRECEDÊNCIA: um mesmo CNPJ pode violar mais de uma regra. Assumi a
 * ordem tamanho -&gt; caracteres permitidos -&gt; dígitos verificadores, pois é
 * a única que produz as mensagens exigidas nos cenários 3, 4 e 5 sem
 * conflito (o payload do cenário 4 também tem tamanho irregular, mas o
 * cenário exige a mensagem de caracteres inválidos).</li>
 *
 * <li>CASO DE SUCESSO: os cenários 1 e 2 não definem mensagem de retorno para
 * o CNPJ válido. Assumi que apenas o resultado booleano é contratual e não
 * criei asserção sobre a mensagem nesses casos, para não inventar
 * requisito.</li>
 *
 * <li>MENSAGENS: assumi que as três mensagens de erro são comparadas
 * literalmente, com acentuação, exatamente como escritas no Gherkin.</li>
 *
 * <li>ABUSE CASE: o cenário 4 exige que "o sistema não deve interromper sua
 * execução". Assumi, portanto, que a entrada maliciosa é rejeitada por
 * retorno normal e NÃO por lançamento de exceção — por isso este cenário usa
 * {@code assertDoesNotThrow} e não {@code assertThrows}.</li>
 * </ul>
 */
@DisplayName("FUNCIONALIDADE: Validador de CNPJ")
class ValidadorCnpjTest {

    private ValidadorCnpj validador;

    @BeforeEach
    void setUp() {
        validador = new ValidadorCnpj();
    }

    @Nested
    @DisplayName("Cenário 1: Caminho Feliz - CNPJ no modelo numérico antigo")
    class Cenario1 {

        @Test
        @DisplayName("Dado um CNPJ numérico válido, quando validado, então o resultado é verdadeiro")
        void deveValidarCnpjNumericoAntigo() {
            // Dado que o usuário deseja informar um CNPJ no formato numérico válido
            String cnpj = "12.345.678/0001-95";

            // Quando solicita a validação do CNPJ
            ResultadoValidacao resultado = validador.validar(cnpj);

            // Então o CNPJ precisa ser válido a partir da nova funcionalidade
            // do CNPJ alfanumérico
            // E o sistema deve retornar o resultado verdadeiro
            assertTrue(resultado.valido(), "CNPJ numérico antigo deve continuar válido");
        }
    }

    @Nested
    @DisplayName("Cenário 2: Caminho Feliz - CNPJ contendo letras e números")
    class Cenario2 {

        @Test
        @DisplayName("Dado um CNPJ alfanumérico válido, quando validado, então o resultado é verdadeiro")
        void deveValidarCnpjAlfanumerico() {
            // Dado que o usuário informa o CNPJ alfanumérico válido
            String cnpj = "12.ABC.345/01DE-35";

            // Quando solicita a validação do CNPJ
            ResultadoValidacao resultado = validador.validar(cnpj);

            // Então o CNPJ deve ser considerado válido
            // E o sistema deve retornar o resultado verdadeiro
            assertTrue(resultado.valido(), "CNPJ alfanumérico válido deve ser aceito");
        }

        @Test
        @DisplayName("E os dois últimos caracteres devem ser reconhecidos como dígitos verificadores numéricos")
        void deveReconhecerOsDoisUltimosCaracteresComoDigitosVerificadores() {
            // Dado que o usuário informa o CNPJ alfanumérico válido
            String cnpj = "12.ABC.345/01DE-35";

            // Quando solicita a extração dos dígitos verificadores
            // Assumi que "os dois últimos caracteres devem ser RECONHECIDOS
            // como dígitos verificadores" exige uma operação observável, e não
            // apenas um efeito interno do validar(). Por isso o esqueleto expõe
            // extrairDigitosVerificadores(): sem ele, este passo do Gherkin não
            // seria testável de forma independente.
            String digitosVerificadores = validador.extrairDigitosVerificadores(cnpj);

            // Então os dois últimos caracteres devem ser dígitos numéricos
            assertEquals("35", digitosVerificadores);
            assertTrue(digitosVerificadores.matches("\\d{2}"),
                    "Os dígitos verificadores devem ser exclusivamente numéricos");
        }
    }

    @Nested
    @DisplayName("Cenário 3: Caminho de Exceção - CNPJ com quantidade insuficiente de caracteres")
    class Cenario3 {

        @Test
        @DisplayName("Dado um CNPJ com 13 caracteres, quando validado, então é inválido com a mensagem 'CNPJ incompleto'")
        void deveRejeitarCnpjComMenosDeQuatorzeCaracteres() {
            // Dado que o usuário informa o CNPJ contendo 13 caracteres
            // Assumi que a contagem de 13 se refere aos caracteres úteis, sem
            // máscara, já que o cenário informa o valor sem pontuação; e que o
            // tamanho esperado é 14 (12 da base + 2 dígitos verificadores).
            String cnpj = "12ABC34501DE3";
            assertEquals(13, cnpj.length(), "Pré-condição do cenário: a entrada tem 13 caracteres");

            // Quando solicita a validação do CNPJ
            ResultadoValidacao resultado = validador.validar(cnpj);

            // Então o CNPJ deve ser considerado inválido
            // E o sistema deve retornar o resultado falso
            assertFalse(resultado.valido(), "CNPJ incompleto não pode ser considerado válido");

            // E deve informar a mensagem "CNPJ incompleto"
            assertEquals("CNPJ incompleto", resultado.mensagem());
        }
    }

    @Nested
    @DisplayName("Cenário 4: Abuse Case - Tentativa de inserir código malicioso no campo de CNPJ")
    class Cenario4 {

        // Assumi aspas simples retas ('). O cenário recebido usa aspas
        // tipográficas (' e "), que são artefato de formatação de texto e não
        // fariam sentido como payload real de injeção SQL.
        private static final String PAYLOAD = "' OR '1'='1";

        @Test
        @DisplayName("Dado um payload de injeção, quando validado, então a entrada é rejeitada com a mensagem de caracteres inválidos")
        void deveRejeitarEntradaMaliciosa() {
            // Dado que um usuário informa o payload de injeção no campo de CNPJ
            // Quando solicita a validação
            ResultadoValidacao resultado = validador.validar(PAYLOAD);

            // Então a entrada deve ser rejeitada
            assertFalse(resultado.valido(), "Entrada maliciosa não pode ser considerada válida");

            // E o sistema deve informar "CNPJ contém caracteres inválidos"
            assertEquals("CNPJ contém caracteres inválidos", resultado.mensagem());
        }

        @Test
        @DisplayName("E o sistema não deve interromper sua execução")
        void naoDeveInterromperAExecucao() {
            // Quando solicita a validação do payload malicioso
            // Então nenhuma exceção pode escapar do validador
            ResultadoValidacao resultado = assertDoesNotThrow(() -> validador.validar(PAYLOAD),
                    "O validador deve tratar a entrada maliciosa sem lançar exceção");

            // E o sistema deve seguir operacional para a próxima validação.
            // Assumi que "não interromper a execução" significa mais do que
            // não lançar exceção: o validador precisa continuar utilizável
            // depois do ataque, então revalido um CNPJ sabidamente bom.
            ResultadoValidacao proximaValidacao = assertDoesNotThrow(
                    () -> validador.validar("12.345.678/0001-95"),
                    "O validador deve continuar operante após a tentativa de ataque");

            assertFalse(resultado.valido());
            assertTrue(proximaValidacao.valido());
        }

        @Test
        @DisplayName("E nenhuma instrução presente na entrada deve ser executada")
        void naoDeveExecutarInstrucoesDaEntrada() {
            // Quando solicita a validação do payload malicioso
            ResultadoValidacao resultado = validador.validar(PAYLOAD);

            // Então a entrada foi tratada como texto: rejeitada pela regra de
            // caracteres permitidos, e não interpretada como instrução.
            // Assumi que "nenhuma instrução deve ser executada" só é
            // verificável de forma indireta em teste unitário: a evidência é a
            // entrada ser rejeitada como dado e não ser refletida na saída.
            assertEquals("CNPJ contém caracteres inválidos", resultado.mensagem(),
                    "A entrada deve ser rejeitada como texto inválido, não interpretada");

            // E o conteúdo da entrada não pode ser refletido de volta na saída
            assertFalse(resultado.mensagem().contains(PAYLOAD),
                    "A mensagem não pode refletir o conteúdo da entrada maliciosa");
            assertFalse(resultado.mensagem().contains("OR"),
                    "Nenhum fragmento da instrução pode aparecer na saída");
        }
    }

    @Nested
    @DisplayName("Cenário 5: Caminho de Exceção - Dígitos verificadores inválidos")
    class Cenario5 {

        @Test
        @DisplayName("Dado um CNPJ com DV incorreto, quando validado, então é inválido com a mensagem 'CNPJ inválido'")
        void deveRejeitarCnpjComDigitosVerificadoresIncorretos() {
            // Dado que o usuário informa o CNPJ alfanumérico com DV incorreto
            String cnpj = "12.ABC.345/01DE-99";

            // Quando solicita a validação do CNPJ
            ResultadoValidacao resultado = validador.validar(cnpj);

            // Então o CNPJ deve ser considerado inválido
            // E o sistema deve retornar o resultado falso
            assertFalse(resultado.valido(), "CNPJ com DV incorreto não pode ser válido");

            // E deve informar a mensagem "CNPJ inválido"
            assertEquals("CNPJ inválido", resultado.mensagem());
        }

        @Test
        @DisplayName("E o sistema deve calcular os dígitos verificadores esperados e identificar a divergência")
        void deveCalcularOsDigitosEsperadosEIdentificarADivergencia() {
            // Dado que o usuário informa o CNPJ alfanumérico com DV incorreto
            String cnpj = "12.ABC.345/01DE-99";

            // Quando o sistema calcula os dígitos verificadores esperados.
            // Assumi que calcularDigitosVerificadores() recebe a base de 12
            // caracteres já sem máscara, porque o cenário fala em "calcular os
            // dígitos ESPERADOS" — ou seja, a partir da base, ignorando os
            // dígitos informados pelo usuário.
            String digitosEsperados = validador.calcularDigitosVerificadores("12ABC34501DE");
            String digitosInformados = validador.extrairDigitosVerificadores(cnpj);

            // Então o sistema deve calcular os dígitos verificadores esperados.
            // Assumi "35" como esperado porque o cenário 2 declara válido o
            // CNPJ "12.ABC.345/01DE-35", que tem exatamente esta mesma base.
            assertEquals("35", digitosEsperados);

            // E deve identificar que os dígitos informados estão incorretos
            assertEquals("99", digitosInformados);
            assertNotEquals(digitosEsperados, digitosInformados,
                    "Os dígitos informados divergem dos calculados");
        }
    }
}

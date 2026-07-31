package br.furb.cnpj;

/**
 * Validador de CNPJ no formato numerico antigo e no novo formato alfanumerico.
 *
 * <p>
 * Fase GREEN do TDD: implementacao da logica que satisfaz o contrato definido
 * pela classe de teste. A classe de teste nao foi alterada; onde o texto dos
 * comentarios dela divergia das assercoes, prevaleceu a assercao, que e o
 * contrato executavel.
 *
 * <p>
 * DECISOES DE IMPLEMENTACAO:
 *
 * <ul>
 * <li>PRECEDENCIA: a ordem aplicada e caracteres permitidos -&gt; tamanho -&gt;
 * digitos verificadores. O javadoc da classe de teste declara a ordem inversa
 * (tamanho antes dos caracteres), mas ela e incompativel com as assercoes: o
 * payload do cenario 4 tem 11 caracteres uteis e ainda assim exige a mensagem
 * "CNPJ contem caracteres invalidos", enquanto o cenario 3 tem 13 caracteres
 * todos validos e exige "CNPJ incompleto". Verificar os caracteres primeiro e
 * a unica ordem que satisfaz os dois cenarios simultaneamente.</li>
 *
 * <li>MASCARA: os separadores {@code . / -} sao descartados antes de qualquer
 * verificacao, de modo que a entrada e aceita com ou sem pontuacao. Exigido
 * pelos cenarios 1, 2 e 5 (que informam o CNPJ pontuado) em conjunto com o
 * cenario 3 (que o informa sem pontuacao).</li>
 *
 * <li>CAIXA: letras minusculas sao normalizadas para maiusculas, pois o CNPJ
 * alfanumerico e definido sobre o alfabeto maiusculo. Nenhum cenario exige
 * isso, mas evita reprovar uma digitacao valida por causa da caixa.</li>
 *
 * <li>MODULO 11: o valor de cada caractere e o seu codigo ASCII menos 48,
 * conforme a regra do CNPJ alfanumerico. Assim os digitos continuam valendo de
 * 0 a 9 e as letras passam a valer de 17 (A) a 42 (Z), o que preserva a
 * compatibilidade com os CNPJ numericos antigos exigida pelo cenario 1.</li>
 *
 * <li>SEGURANCA (abuse case do cenario 4): nenhuma entrada e interpretada,
 * concatenada ou refletida na saida. A validacao percorre a entrada caractere
 * por caractere contra uma lista de permissao (0-9 e A-Z), e as mensagens de
 * retorno sao constantes fixas, jamais montadas a partir da entrada. Nenhum
 * caminho lanca excecao para entrada malformada ou nula, de modo que o
 * validador nunca interrompe a execucao do chamador. O metodo tambem nao
 * guarda estado entre chamadas, portanto uma tentativa de ataque nao contamina
 * a validacao seguinte.</li>
 *
 * <li>ENTRADA NULA E TAMANHO EXCEDENTE: nao sao cobertos por nenhum cenario.
 * Assumi {@code null} e vazio como "CNPJ incompleto", e tamanho maior que 14
 * como "CNPJ invalido", sem lancar excecao em nenhum dos dois casos.</li>
 * </ul>
 */
public class ValidadorCnpj {

    /** Quantidade de caracteres uteis de um CNPJ, sem mascara. */
    private static final int TAMANHO_CNPJ = 14;

    /** Quantidade de caracteres da base, isto e, o CNPJ sem os digitos verificadores. */
    private static final int TAMANHO_BASE = 12;

    /** Quantidade de digitos verificadores. */
    private static final int TAMANHO_DIGITOS_VERIFICADORES = 2;

    /** Caracteres de mascara descartados antes da validacao. */
    private static final String SEPARADORES_DE_MASCARA = "./-";

    /** Deslocamento ASCII usado para converter o caractere em valor numerico. */
    private static final int DESLOCAMENTO_ASCII = 48;

    /** Divisor do algoritmo de modulo 11. */
    private static final int MODULO = 11;

    /** Peso inicial do modulo 11; retorna ao inicial depois de atingir o maximo. */
    private static final int PESO_INICIAL = 2;

    /** Peso maximo do modulo 11. */
    private static final int PESO_MAXIMO = 9;

    /** Restos menores que este valor produzem digito verificador zero. */
    private static final int RESTO_MINIMO = 2;

    private static final String MENSAGEM_VALIDO = "CNPJ valido";
    private static final String MENSAGEM_INCOMPLETO = "CNPJ incompleto";
    private static final String MENSAGEM_CARACTERES_INVALIDOS = "CNPJ contém caracteres inválidos";
    private static final String MENSAGEM_INVALIDO = "CNPJ inválido";

    /**
     * Valida um CNPJ, aceitando ou nao os separadores de mascara
     * ({@code . / -}).
     *
     * @param cnpj CNPJ informado pelo usuario; pode ser nulo
     * @return resultado com o indicador de validade e a mensagem correspondente
     */
    public ResultadoValidacao validar(String cnpj) {
        String normalizado = normalizar(cnpj);

        if (!contemApenasCaracteresPermitidos(normalizado)) {
            return new ResultadoValidacao(false, MENSAGEM_CARACTERES_INVALIDOS);
        }

        if (normalizado.length() < TAMANHO_CNPJ) {
            return new ResultadoValidacao(false, MENSAGEM_INCOMPLETO);
        }

        if (normalizado.length() > TAMANHO_CNPJ) {
            return new ResultadoValidacao(false, MENSAGEM_INVALIDO);
        }

        String base = normalizado.substring(0, TAMANHO_BASE);
        String digitosInformados = normalizado.substring(TAMANHO_BASE);

        // Os dois ultimos caracteres do CNPJ permanecem numericos mesmo no
        // formato alfanumerico.
        if (!apenasDigitos(digitosInformados)) {
            return new ResultadoValidacao(false, MENSAGEM_INVALIDO);
        }

        if (!calcularDigitosVerificadores(base).equals(digitosInformados)) {
            return new ResultadoValidacao(false, MENSAGEM_INVALIDO);
        }

        return new ResultadoValidacao(true, MENSAGEM_VALIDO);
    }

    /**
     * Extrai os dois ultimos caracteres do CNPJ, que sao sempre os digitos
     * verificadores numericos.
     *
     * @param cnpj CNPJ informado pelo usuario; pode ser nulo
     * @return os dois digitos verificadores informados, ou uma string vazia
     *         quando a entrada nao tem caracteres suficientes
     */
    public String extrairDigitosVerificadores(String cnpj) {
        String normalizado = normalizar(cnpj);

        if (normalizado.length() < TAMANHO_DIGITOS_VERIFICADORES) {
            return "";
        }

        return normalizado.substring(normalizado.length() - TAMANHO_DIGITOS_VERIFICADORES);
    }

    /**
     * Calcula os digitos verificadores esperados para a base do CNPJ (os 12
     * primeiros caracteres), conforme o algoritmo do modulo 11 adaptado ao
     * CNPJ alfanumerico.
     *
     * @param base os 12 primeiros caracteres do CNPJ, sem mascara
     * @return os dois digitos verificadores esperados
     * @throws IllegalArgumentException se a base nao tiver exatamente 12
     *         caracteres alfanumericos. Diferente de {@link #validar(String)},
     *         este metodo e um calculo de uso interno, e nao um ponto de
     *         entrada de dados do usuario, portanto sinaliza o uso incorreto em
     *         vez de devolver silenciosamente um valor errado.
     */
    public String calcularDigitosVerificadores(String base) {
        String normalizado = normalizar(base);

        if (normalizado.length() != TAMANHO_BASE || !contemApenasCaracteresPermitidos(normalizado)) {
            throw new IllegalArgumentException(
                    "A base do CNPJ deve conter exatamente " + TAMANHO_BASE + " caracteres alfanumericos");
        }

        int primeiroDigito = calcularDigito(normalizado);
        int segundoDigito = calcularDigito(normalizado + primeiroDigito);

        return String.valueOf(primeiroDigito) + segundoDigito;
    }

    /**
     * Remove a mascara e os espacos das extremidades e normaliza as letras para
     * maiusculas. Entrada nula e tratada como string vazia, para que nenhuma
     * verificacao posterior lance excecao.
     */
    private String normalizar(String cnpj) {
        if (cnpj == null) {
            return "";
        }

        StringBuilder normalizado = new StringBuilder(cnpj.length());
        for (char caractere : cnpj.trim().toCharArray()) {
            if (SEPARADORES_DE_MASCARA.indexOf(caractere) < 0) {
                normalizado.append(Character.toUpperCase(caractere));
            }
        }

        return normalizado.toString();
    }

    /**
     * Aplica a lista de permissao: um CNPJ alfanumerico admite apenas digitos
     * de 0 a 9 e letras de A a Z. Qualquer outro caractere (espaco, aspas,
     * operadores, pontuacao) reprova a entrada sem que ela seja interpretada.
     */
    private boolean contemApenasCaracteresPermitidos(String cnpjSemMascara) {
        if (cnpjSemMascara.isEmpty()) {
            return false;
        }

        for (char caractere : cnpjSemMascara.toCharArray()) {
            boolean digito = caractere >= '0' && caractere <= '9';
            boolean letra = caractere >= 'A' && caractere <= 'Z';
            if (!digito && !letra) {
                return false;
            }
        }

        return true;
    }

    /** Indica se todos os caracteres informados sao digitos de 0 a 9. */
    private boolean apenasDigitos(String caracteres) {
        if (caracteres.isEmpty()) {
            return false;
        }

        for (char caractere : caracteres.toCharArray()) {
            if (caractere < '0' || caractere > '9') {
                return false;
            }
        }

        return true;
    }

    /**
     * Aplica o modulo 11 sobre os caracteres informados, da direita para a
     * esquerda, com pesos de 2 a 9 que reiniciam apos o 9.
     */
    private int calcularDigito(String caracteres) {
        int soma = 0;
        int peso = PESO_INICIAL;

        for (int i = caracteres.length() - 1; i >= 0; i--) {
            soma += valorDe(caracteres.charAt(i)) * peso;
            peso = peso < PESO_MAXIMO ? peso + 1 : PESO_INICIAL;
        }

        int resto = soma % MODULO;

        return resto < RESTO_MINIMO ? 0 : MODULO - resto;
    }

    /** Valor numerico do caractere: o codigo ASCII menos 48. */
    private int valorDe(char caractere) {
        return caractere - DESLOCAMENTO_ASCII;
    }
}

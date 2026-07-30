package br.furb.cnpj;

/**
 * Validador de CNPJ no formato numerico antigo e no novo formato alfanumerico.
 *
 * <p>
 * ESQUELETO - fase RED do TDD. As assinaturas existem apenas para que os
 * testes compilem e cada metodo devolve um valor neutro, sem nenhuma regra de
 * negocio. Assim os testes reprovam por assercao (Failure), e nao por excecao
 * (Error).
 */
public class ValidadorCnpj {

    /** Valor neutro devolvido enquanto a implementacao nao existe. */
    private static final String NAO_IMPLEMENTADO = "";

    /**
     * Valida um CNPJ, aceitando ou nao os separadores de mascara
     * ({@code . / -}).
     *
     * @param cnpj CNPJ informado pelo usuario
     * @return resultado com o indicador de validade e a mensagem correspondente
     */
    public ResultadoValidacao validar(String cnpj) {
        // TODO fase GREEN: verificar o tamanho, os caracteres permitidos e os
        // digitos verificadores, devolvendo a mensagem adequada.
        return new ResultadoValidacao(false, NAO_IMPLEMENTADO);
    }

    /**
     * Extrai os dois ultimos caracteres do CNPJ, que sao sempre os digitos
     * verificadores numericos.
     *
     * @param cnpj CNPJ informado pelo usuario
     * @return os dois digitos verificadores informados
     */
    public String extrairDigitosVerificadores(String cnpj) {
        // TODO fase GREEN: remover a mascara e devolver os dois ultimos digitos.
        return NAO_IMPLEMENTADO;
    }

    /**
     * Calcula os digitos verificadores esperados para a base do CNPJ (os 12
     * primeiros caracteres), conforme o algoritmo do modulo 11 adaptado ao
     * CNPJ alfanumerico.
     *
     * @param base os 12 primeiros caracteres do CNPJ, sem mascara
     * @return os dois digitos verificadores esperados
     */
    public String calcularDigitosVerificadores(String base) {
        // TODO fase GREEN: aplicar o modulo 11 usando o valor ASCII - 48 de
        // cada caractere da base.
        return NAO_IMPLEMENTADO;
    }
}

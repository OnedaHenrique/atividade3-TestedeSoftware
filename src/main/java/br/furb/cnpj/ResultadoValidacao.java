package br.furb.cnpj;

/**
 * Resultado da validacao de um CNPJ.
 *
 * @param valido   verdadeiro quando o CNPJ passou em todas as verificacoes
 * @param mensagem mensagem descritiva devolvida ao usuario
 */
public record ResultadoValidacao(boolean valido, String mensagem) {
}

package generator;

import data.InicializadorDados;
import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;
import models.Materia;
import models.Professor;

public class PopInicial {

    private static final Random random = new Random();
    private static final Scanner scanner = new Scanner(System.in);
    private static final List<Professor> professores = InicializadorDados.criarProfessores();
    private static final Map<String, List<Materia>> materiasPorPeriodo = InicializadorDados.criarMateriasPorPeriodo();

    public static void main(String[] args) {
        List<String[][]> populacaoInicial;
        Map<Materia, Professor> associacoes = associarProfessoresAMaterias();
        imprimirTabelaAssociacoes(associacoes);
        populacaoInicial = gerarPopulacaoInicial(associacoes, 50);
        gerarTabelaHTML(associacoes, populacaoInicial);
    }

    public static Map<Materia, Professor> associarProfessoresAMaterias() {
        Map<Materia, Professor> associacoes = new HashMap<>();
        Map<String, List<Professor>> professoresPorPeriodo = new HashMap<>();
    
        // Inicializa a lista de professores por período
        materiasPorPeriodo.forEach((periodo, materias) -> {
            professoresPorPeriodo.put(periodo, new ArrayList<>(professores));
        });
    
        System.out.println("Deseja fazer a associação de professores a matérias de forma aleatória? (sim/nao)");
        String resposta = scanner.nextLine();
        boolean aleatorio = resposta.equalsIgnoreCase("sim");
    
        // Realiza a associação das matérias aos professores
        materiasPorPeriodo.forEach((periodo, materias) -> {
            System.out.println("\nPeríodo " + periodo);
            for (Materia materia : materias) {
                Professor professorEscolhido;
                if (aleatorio) {
                    // Se aleatório, escolhe um professor que ainda não tenha sido atribuído ao período
                    List<Professor> professoresDisponiveis = professoresPorPeriodo.get(periodo);
                    professorEscolhido = professoresDisponiveis.get(random.nextInt(professoresDisponiveis.size()));
                    // Remove o professor do período para não ser escolhido novamente
                    professoresDisponiveis.remove(professorEscolhido);
                } else {
                    // Caso o usuário escolha manualmente
                    System.out.println("Escolha o professor para a matéria " + materia.getNome() + ":");
                    for (int i = 0; i < professores.size(); i++) {
                        System.out.printf("%d. %s (%s)\n", i + 1, professores.get(i).getNome(), professores.get(i).getCodigo());
                    }
                    int escolha = scanner.nextInt() - 1;
                    scanner.nextLine();
                    professorEscolhido = professores.get(escolha);
                    // Remove o professor do período para não ser escolhido novamente
                    professoresPorPeriodo.get(periodo).remove(professorEscolhido);
                }
    
                // Associa a matéria ao professor
                associacoes.put(materia, professorEscolhido);
            }
        });
    
        return associacoes;
    }
    

    public static void imprimirTabelaAssociacoes(Map<Materia, Professor> associacoes) {
        // Ordena as matérias pelo código
        List<Map.Entry<Materia, Professor>> sortedAssociations = new ArrayList<>(associacoes.entrySet());
        sortedAssociations.sort((entry1, entry2) -> entry1.getKey().getCodigo().compareTo(entry2.getKey().getCodigo()));
    
        System.out.printf("| %-7s | %-32s | %-12s | %-16s | %-9s | %-10s |%n",
                "Período", "Matéria", "Cod. Matéria", "Professor", "Cod. Prof", "Cod. Comb.");
        System.out.println("|---------|----------------------------------|--------------|------------------|-----------|------------|");
    
        // Imprime as associações ordenadas
        for (Map.Entry<Materia, Professor> entry : sortedAssociations) {
            Materia materia = entry.getKey();
            Professor professor = entry.getValue();
            String codCombinado = professor.getCodigo() + materia.getCodigo();
    
            System.out.printf("| %-7s | %-32s | %-12s | %-16s | %-9s | %-10s |%n",
                    materia.getPeriodo(),
                    materia.getNome(),
                    materia.getCodigo(),
                    professor.getNome(),
                    professor.getCodigo(),
                    codCombinado);
        }
    }
    

    public static List<String[][]> gerarPopulacaoInicial(Map<Materia, Professor> associacoes, int quantidade) {
        List<String[][]> populacao = new ArrayList<>();

        Map<String, List<String>> codigosPorPeriodo = new TreeMap<>();
        for (Map.Entry<Materia, Professor> entry : associacoes.entrySet()) {
            String periodo = entry.getKey().getPeriodo();
            String codCombinado = entry.getValue().getCodigo() + entry.getKey().getCodigo();
            codigosPorPeriodo.putIfAbsent(periodo, new ArrayList<>());
            for (int i = 0; i < 4; i++) {
                codigosPorPeriodo.get(periodo).add(codCombinado);
            }
        }

        for (int i = 0; i < quantidade; i++) {
            List<String> linha = new ArrayList<>();
            for (String periodo : codigosPorPeriodo.keySet()) {
                List<String> codigos = new ArrayList<>(codigosPorPeriodo.get(periodo));
                Collections.shuffle(codigos);
                linha.addAll(codigos);
            }
            String[][] individuo = new String[1][100];
            for (int j = 0; j < 100; j++) {
                individuo[0][j] = linha.get(j);
            }
            populacao.add(individuo);
        }

        return populacao;
    }

    public static void gerarTabelaHTML(Map<Materia, Professor> associacoes, List<String[][]> populacaoInicial) {
        StringBuilder html = new StringBuilder();
    
        html.append("<!DOCTYPE html><html lang='pt-BR'><head>");
        html.append("<meta charset='UTF-8'><title>Tabela de Horários</title>");
        html.append("<style>");
        html.append("html { overflow-x: auto; }");
        html.append("body { font-family: Arial; margin: 40px; }");
        html.append("table { border-collapse: collapse; min-width: 1200px; font-size: 12px; }");
        html.append("th, td { border: 1px solid #888; padding: 6px; text-align: center; }");
        html.append("th { background-color: #f2f2f2; }");
        html.append("</style></head><body>");
    
        html.append("<h2>Tabela de Associação Professor x Matéria</h2>");
        html.append("<table>");
        html.append("<tr><th>Período</th><th>Matéria</th><th>Cód. Matéria</th><th>Professor</th><th>Cód. Prof</th><th>Cód. Comb.</th></tr>");
    
        // Ordena as matérias por código
        List<Map.Entry<Materia, Professor>> listaAssociacoes = new ArrayList<>(associacoes.entrySet());
        listaAssociacoes.sort((entry1, entry2) -> entry1.getKey().getCodigo().compareTo(entry2.getKey().getCodigo()));
    
        // Imprime as matérias e os professores em ordem
        for (Map.Entry<Materia, Professor> entry : listaAssociacoes) {
            Materia m = entry.getKey();
            Professor p = entry.getValue();
            String codCombinado = p.getCodigo() + m.getCodigo();
    
            html.append("<tr>")
                .append("<td>").append(m.getPeriodo()).append("</td>")
                .append("<td>").append(m.getNome()).append("</td>")
                .append("<td>").append(m.getCodigo()).append("</td>")
                .append("<td>").append(p.getNome()).append("</td>")
                .append("<td>").append(p.getCodigo()).append("</td>")
                .append("<td>").append(codCombinado).append("</td>")
                .append("</tr>");
        }
    
        html.append("</table>");
    
        html.append("<h2 style='margin-top:40px;'>População Inicial (50 indivíduos com 100 genes)</h2>");
        html.append("<div><table><thead><tr>");
        for (int i = 1; i <= 100; i++) {
            html.append("<th>").append(i).append("</th>");
        }
        html.append("</tr></thead><tbody>");
        for (String[][] individuo : populacaoInicial) {
            html.append("<tr>");
            for (int j = 0; j < 100; j++) {
                html.append("<td>").append(individuo[0][j]).append("</td>");
            }
            html.append("</tr>");
        }
        html.append("</tbody></table></div></body></html>");
    
        try {
            String nomeArquivo = "horario.html";
            BufferedWriter writer = new BufferedWriter(new FileWriter(nomeArquivo));
            writer.write(html.toString());
            writer.close();
    
            File htmlFile = new File(nomeArquivo);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(htmlFile.toURI());
            } else {
                System.out.println("Abra manualmente o arquivo: " + htmlFile.getAbsolutePath());
            }
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}

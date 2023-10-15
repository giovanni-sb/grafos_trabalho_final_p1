import java.io.*;
import java.util.regex.*;
import model.*;

public class ManipulacaoArquivos {
    public static void main(String[] args) {
        String pastaPrincipal = "Teste";
        criarDiretorios(pastaPrincipal);
        processarArquivosRota(pastaPrincipal);
    }

    public static void criarDiretorios(String pastaPrincipal) {
        String configuracaoPath = pastaPrincipal + "/Configuracao/config.txt";

        try {
            BufferedReader reader = new BufferedReader(new FileReader(configuracaoPath));
            String line;
            String processadoDir = null;
            String naoProcessadoDir = null;

            while ((line = reader.readLine()) != null) {
                if (line.contains("Processado=")) {
                    processadoDir = line.replace("Processado=", "");
                } else if (line.contains("Não Processado=")) {
                    naoProcessadoDir = line.replace("Não Processado=", "");
                }
            }

            if (processadoDir != null) {
                criarDiretorio(processadoDir);
            }
            if (naoProcessadoDir != null) {
                criarDiretorio(naoProcessadoDir);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void criarDiretorio(String path) {
        File diretorio = new File(path);
        if (!diretorio.exists()) {
            if (diretorio.mkdirs()) {
                System.out.println("Diretório criado: " + path);
            } else {
                System.err.println("Erro ao criar o diretório: " + path);
            }
        }
    }

    public static void processarArquivosRota(String pastaPrincipal) {
        File[] arquivos = new File(pastaPrincipal).listFiles();
        if (arquivos != null) {
            for (File arquivo : arquivos) {
                if (arquivo.getName().startsWith("rota") && arquivo.getName().endsWith(".txt")) {
                    try {
                        processarArquivoRota(arquivo);
                        File destino = new File(pastaPrincipal + "/Processado/" + arquivo.getName());
                        arquivo.renameTo(destino);
                    } catch (Exception e) {
                        File destino = new File(pastaPrincipal + "/NãoProcessado/" + arquivo.getName());
                        arquivo.renameTo(destino);
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void processarArquivoRota(File arquivo) {
        int totalNoArquivo = 0;
        int somaPesos = 0;
        Grafo grafo = new Grafo();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(arquivo));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("00")) {
                    // Processar linha de cabeçalho
                    totalNoArquivo = Integer.parseInt(line.substring(2,4));
                    somaPesos = Integer.parseInt(line.substring(4));
                } else if (line.startsWith("01")) {
                    // Processar linha de resumo de conexões
                    String linhaNo = line.substring(2);
                    String nomeNoOrigem = linhaNo.split("=")[0];
                    String nomeNoDestino = linhaNo.split("=")[1];
                    if(!grafo.hasVertice(nomeNoOrigem)){
                        grafo.addVertice(nomeNoOrigem);
                    }
                    if(!grafo.hasVertice(nomeNoDestino)){
                        grafo.addVertice(nomeNoDestino);
                    }
                } else if (line.startsWith("02")) {
                    // Processar linha de resumo de pesos
                } else if (line.startsWith("09")) {
                    // Processar linha de trailer
                } 
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

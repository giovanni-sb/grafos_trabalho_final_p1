import java.io.*;
import model.*;

public class ManipulacaoArquivos {
    public static void main(String[] args) {
        String diretorioAtual = System.getProperty("user.dir");
        String pastaPrincipal = "Teste";
        criarDiretorios(diretorioAtual + File.separator + pastaPrincipal);
        processarArquivosRota(pastaPrincipal);
    }

    public static void criarDiretorios(String pastaPrincipal) {
        String configuracaoPath = pastaPrincipal + File.separator + "Configuracao";
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(configuracaoPath+File.separator+"config.txt"));
            String line;
            String processadoDir = null;
            String naoProcessadoDir = null;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Processado=")) {
                    processadoDir = line.replace("Processado=", "");
                } else if (line.startsWith("Não Processado=")) {
                    naoProcessadoDir = line.replace("Não Processado=", "");
                }
            }

            if (processadoDir != null) {
                criarDiretorio(processadoDir);
            }
            if (naoProcessadoDir != null) {
                criarDiretorio(naoProcessadoDir);
            }
        } catch (FileNotFoundException e) {
            criarDiretorio(configuracaoPath);
            criarArquivoConfig(configuracaoPath);
            criarDiretorios(pastaPrincipal);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void criarArquivoConfig(String configuracaoPath) {
        File arquivo = new File(configuracaoPath+File.separator+"config.txt");
        try {
            if (arquivo.createNewFile()){
                try {
                    FileWriter writer = new FileWriter(arquivo);
                    writer.write("Processado=Teste"+File.separator+"Processado"+System.lineSeparator()+"Não Processado=Teste"+File.separator+"NaoProcessado"+System.lineSeparator());
                    writer.close();
                } catch (IOException e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
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
        } else {
            System.out.println("Nenhum arquivo de rota encontrado no caminho:\n"+
            pastaPrincipal);
        }
    }

    public static void processarArquivoRota(File arquivo) throws Exception {
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
                    //adiciona vertice caso não exista 
                    Vertice noOrigem, noDestino;
                    if(!grafo.hasVertice(nomeNoOrigem)){
                        noOrigem = grafo.addVertice(nomeNoOrigem);
                    } else {
                        noOrigem = grafo.getVerticeByName(nomeNoOrigem);
                    }
                    if(!grafo.hasVertice(nomeNoDestino)){
                        noDestino = grafo.addVertice(nomeNoDestino);
                    } else {
                        noDestino = grafo.getVerticeByName(nomeNoDestino);
                    }
                    adicionaAresta(grafo, noOrigem, noDestino, 0);
                } else if (line.startsWith("02")) {
                    // Processar linha de resumo de pesos
                    String linhaPeso = line.substring(2);
                    String nomeNoOrigem = linhaPeso.split(";")[0];
                    String nomeNoDestino = linhaPeso.split(";")[1].split("=")[0];
                    String pesoArestaString = linhaPeso.split("=")[1];
                    int pesoAresta = Integer.parseInt(pesoArestaString);
                    Vertice noOrigem = grafo.getVerticeByName(nomeNoOrigem);
                    Vertice noDestino = grafo.getVerticeByName(nomeNoDestino);
                    //adicona o peso numa aresta existente
                    adicionaPesoAresta(grafo, noOrigem, noDestino, pesoAresta);
                } else if (line.startsWith("09")) {
                    // Processar linha de trailer
                    String linhaTrailer = line.substring(2);
                    String linhaRC = linhaTrailer.split(";")[0];
                    String linhaRP = linhaTrailer.split(";")[1];
                    String linhaPesoTotal = linhaTrailer.split(";")[2];
                    int resumoConexao = Integer.parseInt(linhaRC.split("=")[1]);
                    int resumoPesos = Integer.parseInt(linhaRP.split("=")[1]);
                } else {
                    throw new Exception("Erro ao ler linha do arquivo rota, formato da linha incompativel: "+line);
                }
            }
            reader.close();
            System.out.println(grafo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void adicionaAresta(Grafo grafo, Vertice origem, 
                                        Vertice destino, int peso){
        grafo.addAresta(origem, destino, peso);
    }

    public static void adicionaPesoAresta(Grafo grafo, Vertice origem, 
                                        Vertice destino, int peso){
        grafo.getAresta(origem, destino).setPeso(peso);
    }
}

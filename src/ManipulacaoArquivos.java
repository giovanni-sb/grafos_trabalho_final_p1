import java.io.*;
import model.*;

public class ManipulacaoArquivos {
    public static void main(String[] args) {
        String diretorioAtual = System.getProperty("user.dir");
        String pastaPrincipal = "Teste";
        try {
            criarDiretorios(diretorioAtual + File.separator + pastaPrincipal);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        processarArquivosRota(pastaPrincipal);
    }

    public static void criarDiretorios(String pastaPrincipal) throws Exception {
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
            if (processadoDir == null){
                throw new Exception("Arquivo config.txt não possuí destino para arquivos processados corretamente");
            } else if(processadoDir != null) {
                criarDiretorio(processadoDir);
            }
            if (naoProcessadoDir == null){
                throw new Exception("Arquivo config.txt não possuí destino para arquivos não processados corretamente");
            } else if (naoProcessadoDir != null) {
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
                        File destino = new File(pastaPrincipal +File.separator+ "Processado" +File.separator+ arquivo.getName());
                        arquivo.renameTo(destino);
                    } catch (Exception e) {
                        File destino = new File(pastaPrincipal +File.separator+ "NaoProcessado" +File.separator+ arquivo.getName());
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
        int somaPesosHeader = 0;
        int somaPesosLinhas = 0;
        int numeroLinhasConexao = 0;
        int numeroLinhasPesos = 0;
        int resumoConexao = 0;
        int resumoPesos = 0;
        int somaPesosTrailer = 0;
        int caractersHeader = 0;
        int totalNoHeader = 0;
        Grafo grafo = new Grafo();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(arquivo));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("00")) {
                    // Processar linha de cabeçalho
                    caractersHeader = line.length();
                    totalNoHeader = Integer.parseInt(line.substring(2,4));
                    somaPesosHeader = Integer.parseInt(line.substring(4));
                } else if (line.startsWith("01")) {
                    // Processar linha de resumo de conexões
                    String linhaNo = line.substring(2);
                    String nomeNoOrigem = null;
                    String nomeNoDestino = null;
                    try {
                        nomeNoOrigem = linhaNo.split("=")[0];
                        nomeNoDestino = linhaNo.split("=")[1];
                        if (nomeNoOrigem.length() > 2 || nomeNoDestino.length() >2) {
                            throw new Exception("Tamanho do nome do nó inválido, limite 2 caracteres");
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        throw new Exception("Linha do resumo de conexões inválida: "+line);
                    }
                    //adiciona vertice caso não exista 
                    Vertice noOrigem, noDestino;
                    if(!grafo.hasVertice(nomeNoOrigem)){
                        noOrigem = grafo.addVertice(nomeNoOrigem);
                        totalNoArquivo++;
                    } else {
                        noOrigem = grafo.getVerticeByName(nomeNoOrigem);
                    }
                    if(!grafo.hasVertice(nomeNoDestino)){
                        noDestino = grafo.addVertice(nomeNoDestino);
                        totalNoArquivo++;
                    } else {
                        noDestino = grafo.getVerticeByName(nomeNoDestino);
                    }
                    adicionaAresta(grafo, noOrigem, noDestino, 0);
                    numeroLinhasConexao += 1;
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
                    somaPesosLinhas += pesoAresta;
                    numeroLinhasPesos += 1;
                } else if (line.startsWith("09")) {
                    // Processar linha de trailer
                    String linhaTrailer = line.substring(2);
                    String linhaRC = linhaTrailer.split(";")[0];
                    String linhaRP = linhaTrailer.split(";")[1];
                    resumoConexao = Integer.parseInt(linhaRC);
                    resumoPesos = Integer.parseInt(linhaRP);
                } else {
                    throw new Exception("Erro ao ler linha do arquivo rota, formato da linha incompativel: "+line);
                }
            }
            if (caractersHeader > 9){
                throw new Exception("Header inválido.");
            }
            if (somaPesosHeader != somaPesosLinhas) {
                throw new Exception("Soma dos pesos difere (Valor do Registro HEADER = "+ somaPesosHeader +" e Soma total dos Pesos = "+ somaPesosLinhas +"): ");
            }
            if (totalNoHeader != totalNoArquivo){
                throw new Exception("Número totais de nós inválido.");
            }

            if (numeroLinhasConexao != resumoConexao) {
                throw new Exception("Número de linhas do resumo de conexão ("+numeroLinhasConexao+") não bate com o informado no TRAILER ("+resumoConexao+")");
            }
            if (numeroLinhasPesos != resumoPesos) {
                throw new Exception("Número de linhas do resumo de pesos ("+numeroLinhasPesos+") não bate com o informado no TRAILER ("+resumoPesos+")");
            }
            reader.close();
            //System.out.println(grafo);
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

package animator;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Automato {
	public ArrayList<String> Estados;
	public ArrayList<String> EstadosIniciais;
	public ArrayList<String> EstadosFinais;
	public Map<String, String> Transicoes;
	public Map<String, List<String>> AFN;    
	public ArrayList<String> Alfabeto;
	public ArrayList<String> Destino;
	public ArrayList<String> ImprimirDot;
	public ArrayList<String> ImprimirDotTransicao;
	public String EstadoAtual;
	public List<String> EstadoAtualAFN;
	public boolean aceito = false;
	
//--------------------------------------- AFN --------------------------------------------------------------
	public void AFN(String[] linha1, ArrayList<String> transicao1, String palavra) {
		this.Estados = new ArrayList<String>();
		this.EstadosIniciais = new ArrayList<String>();
		this.EstadosFinais = new ArrayList<String>();
		this.EstadoAtualAFN = new ArrayList<String>();
		this.Alfabeto = new ArrayList<String>();
		this.ImprimirDot = new ArrayList<String>();
		this.ImprimirDotTransicao = new ArrayList<String>();

		String t;String[] t1;
		String i = linha1[0];String f = linha1[1];
		String[] iniciais = i.split(" "); String[] finais = f.split(" ");
		
		for(int x=0;x<iniciais.length; x++) {//Monta estados iniciais
			EstadosIniciais.add(iniciais[x]);
		}
		for(int x=1;x<finais.length; x++) {//Monta estados finais
			EstadosFinais.add(finais[x]);
		}
		for(int x=0;x<transicao1.size();x++) {//Monta estados e alfabeto
			t = transicao1.get(x);
			t = t.replace(">", "");t = t.replace("  ", " ");//remove os caracteres desnecessarios
			t1 = t.split(" ");	// splita em vetor [origem,char,destino]
			if(!(Estados.contains(t1[0]))) {
				Estados.add(t1[0]);
			}
			if(!(Estados.contains(t1[2]))) {
				Estados.add(t1[2]);
			}
			if(!(Alfabeto.contains(t1[1]))) {
				Alfabeto.add(t1[1]);
			}
		}
		
		montarAFN(transicao1);
		System.out.println("Estados => "+Estados+" | Transições => "+AFN+" | Alfabeto => "+Alfabeto);
		this.aceito = executarAFN(palavra);
		
		if(aceito){
			System.out.println("|---    Estado Final: (" +EstadoAtual +")    ---|");
			System.out.println("|-----    Palavra Aceita    -----|");
		} else {
			System.out.println("|---    Estado Final: (" +EstadoAtual +")    ---|");
			System.out.println("|-----  Palavra Rejeitada   -----|");
		}
		
		//cria arquivo dot inicial e final
		try {
			criarDOT();
			criarDOTFinal(EstadoAtual);
			System.out.println("|-------   DOT  Criado    -------|");
			Thread.sleep(2000);
			Runtime.getRuntime().exec("magick convert -delay 120 -loop 0 *.png automato.gif");	// converte para gif
			Thread.sleep(3000);
			System.out.println("|-----   Animação Criada    -----|");
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}

	public void montarAFN(ArrayList<String> transicao1) {//Monta map de transicoes
		this.AFN = new HashMap<String, List<String>>();
		String t;String[] t1;String s;String dot;
		for(int x=0;x<transicao1.size();x++) {//chave para cada transicao no formato -> "[ESTADO]:CHAR"
			t = transicao1.get(x);
			t = t.replace(">", "");t = t.replace("  ", " ");//remove os caracteres desnecessarios
			t1 = t.split(" ");		// splita em vetor [origem,char,destino]
			s = "["+t1[0]+"]:"+t1[1];	//[ESTADO]:CHAR"
			AFN.putIfAbsent(s, new ArrayList<String>());
			AFN.get(s).add(t1[2]);		//associa destino a [ESTADO]:VALOR/CHAR
			dot = t1[0]+" -> "+t1[2]+" [ label="+t1[1]+"];";//adiciona dot da transmicao
			this.ImprimirDot.add(dot);						//
		}	
	}
	
	public boolean executarAFN(String palavra) {//executar
		if(EstadosIniciais.size()==1) {	// se so um estado inicial
			this.EstadoAtualAFN.add(EstadosIniciais.get(0));//inicial
			System.out.println("|--- Estado Inicial: " +EstadoAtualAFN +" | Palavra: "+palavra+" ---|");
			System.out.println("_________________________________________________________________________________________________________________");
			
			//comeco do automato
			for (int i=0; i<palavra.length(); i++) {//comeca a consumir palavra
				this.Destino = new ArrayList<String>();
				String transicao;String dot;	
				Character c = palavra.charAt(i);
	
				for (int j =0;j<EstadoAtualAFN.size();j++) {//transicao do atual para seu destino
					transicao = "["+EstadoAtualAFN.get(j)+"]:"+c;//monta key transicao para buscar no map de transicoes
					if (AFN.get(transicao) != null) { //se existe transicao pelo caminho escolhido movimenta
						Destino.addAll(AFN.get(transicao)); //destinos possiveis
						if(Destino.size()>1) {	//se mais de um caminho possivel (Nao-Determinismo)
							printTransicaoAFN2(EstadoAtualAFN.get(j), c, Destino);
							try {
								criarDOTTransicaoND(i,EstadoAtualAFN.get(j),c,Destino);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {	//se so um caminho possivel (Determinismo)
							printTransicaoAFN(EstadoAtualAFN.get(j), c, Destino);
							try {
								criarDOTTransicao(i,EstadoAtualAFN.get(j),c,Destino.get(0));
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					} else {	//transicao vazia, nao da pra proseguir nesse caminho (Nao-Determinismo)
						System.out.print("|---    Sem caminho   ---| ");printTransicaoAFN2(EstadoAtualAFN.get(j), c, Destino);
					}
				}
				if (Destino.isEmpty()) { //nao achou nenhum destino possivel rejeito
					System.out.print("|---    Sem caminho   ---| ");
					return false;
				}
				EstadoAtualAFN = Destino;	//a cada movimentacao por um caminho valido do automato
				System.out.println("_________________________________________________________________________________________________________________");
			}
			
			System.out.println("|-----  Leu toda a palavra  -----|");
			
			int i=0;
			for (i=0;i<EstadoAtualAFN.size();i++) {	//Se EstadosFinais contem qualquer dos estados atuais
				if (EstadosFinais.contains(EstadoAtualAFN.get(i))) {
					this.EstadoAtual = EstadoAtualAFN.get(i);//estado final encontrado, aceitou
					return true;
				}
			}
			
			this.EstadoAtual = EstadoAtualAFN.get(0);
			return false;
			
		} else {	//se mais de um estado inicial
			System.out.println("|-----  Mais de um estado inicial  -----|");
			for(int x=0;x<EstadosIniciais.size();x++) {
				EstadoAtualAFN.clear();
				this.EstadoAtualAFN.add(EstadosIniciais.get(x));//inicial
				System.out.println("|--- Estado Inicial: " +EstadoAtualAFN +" | Palavra: "+palavra+" ---|");
				System.out.println("_________________________________________________________________________________________________________________");
				
				//comeco do automato
				for (int i=0; i<palavra.length(); i++) {//comeca a consumir palavra
					this.Destino = new ArrayList<String>();
					String transicao;String dot;	
					Character c = palavra.charAt(i);
		
					for (int j =0;j<EstadoAtualAFN.size();j++) {//transicao do atual para seu destino
						transicao = "["+EstadoAtualAFN.get(j)+"]:"+c;//monta key transicao para buscar no map de transicoes
						if (AFN.get(transicao) != null) { //se existe transicao pelo caminho escolhido movimenta
							Destino.addAll(AFN.get(transicao)); //destinos possiveis
							if(Destino.size()>1) {	//se mais de um caminho possivel (Nao-Determinismo)
								printTransicaoAFN2(EstadoAtualAFN.get(j), c, Destino);
								try {
									criarDOTTransicaoND(i,EstadoAtualAFN.get(j),c,Destino);
								} catch (FileNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							} else {	//se so um caminho possivel (Determinismo)
								printTransicaoAFN(EstadoAtualAFN.get(j), c, Destino);
								try {
									criarDOTTransicao(i,EstadoAtualAFN.get(j),c,Destino.get(0));
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						} else {	//transicao vazia, nao da pra proseguir nesse caminho (Nao-Determinismo)
							System.out.print("|---    Sem caminho   ---| ");printTransicaoAFN2(EstadoAtualAFN.get(j), c, Destino);
						}
					}
					if (Destino.isEmpty()) { //nao achou nenhum destino possivel rejeito
						System.out.print("|---    Sem caminho   ---| ");
						return false;
					}
					EstadoAtualAFN = Destino;	//a cada movimentacao por um caminho valido do automato
					System.out.println("_________________________________________________________________________________________________________________");
				}
				
				System.out.println("|-----  Leu toda a palavra  -----|");
				
				int i=0;
				for (i=0;i<EstadoAtualAFN.size();i++) {	//Se EstadosFinais contem qualquer dos estados atuais
					if (EstadosFinais.contains(EstadoAtualAFN.get(i))) {
						this.EstadoAtual = EstadoAtualAFN.get(i);//estado final encontrado, aceitou
						return true;
					}
				}
				
				this.EstadoAtual = EstadoAtualAFN.get(0);
			}
			return false;
		}
	}

	private void printTransicaoAFN(String string, Character c, List<String> e2) {
		System.out.println("{ ESTADO ATUAL: " + string + " | CHAR LIDO: " + c + " | DESTINO-> " + e2 +"}");
		
	}
	private void printTransicaoAFN2(String e1, Character c, List<String> e2) {
		System.out.println("{ NÃO DETERMINISMO -> ESTADO ATUAL: " + e1 + " | CHAR LIDO: " + c + " | DESTINO-> " + e2 +"}");
	}
	
	private void criarDOT() throws IOException{//dot -Tpng g.dot -o grafo1.png
		try(BufferedWriter out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream("g.dot"))))
		{
		    out.write("digraph {"); out.newLine();
		    out.write("{"); out.newLine();
		    for(int i=0;i<EstadosIniciais.size();i++) {
		    	out.write(EstadosIniciais.get(i)+"[margin=0 color=cornflowerblue fontcolor=white fontsize=16 width=0.5 shape=circle style=filled]"); out.newLine();
		    }
		    for(int i=0;i<EstadosFinais.size();i++) {
		    	out.write(EstadosFinais.get(i)+"[margin=0 color=cornflowerblue fontcolor=white fontsize=16 width=0.5 shape=doublecircle style=filled]"); out.newLine();
		    }
		    for(int i=0;i<Estados.size();i++) {
		    	if(!EstadosFinais.contains(Estados.get(i))) {
		    		if(!EstadosIniciais.contains(Estados.get(i))) {
		    			out.write(Estados.get(i)+"[margin=0 color=cornflowerblue fontcolor=white fontsize=16 width=0.5 shape=circle style=filled]");out.newLine();
		    		}
		    	}	    		 
		    }
		    out.write("}");
		    out.newLine();
		    for(int i=0;i<EstadosIniciais.size();i++) {
		    	 out.write("start -> "+EstadosIniciais.get(i));out.newLine();
		    }
		   
		    for(int i=0;i<ImprimirDot.size();i++) {
		    	out.write(ImprimirDot.get(i));
		    	//System.out.println(ImprimirDot.get(i));
		    	out.newLine();
		    }
	    
		    out.write("start [shape=Mdiamond];");
		    out.newLine();
		    out.write("}");
		    
		    Runtime.getRuntime().exec("dot -Tpng g.dot -o grafo.png");
		 }
	}
	
	private void criarDOTTransicao(int x, String Estado, Character c, String Destino) throws IOException{//dot -Tpng g.dot -o grafo1.png
		String s="";String dot = Estado+" -> "+Destino+" [ label="+c+"];";
		try(BufferedWriter out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream("g"+x+".dot"))))
		{
		    out.write("digraph {"); out.newLine();
		    out.write("{"); out.newLine();
		    for(int i=0;i<EstadosIniciais.size();i++) {
		    	out.write(EstadosIniciais.get(i)+"[margin=0 color=cornflowerblue fontcolor=white fontsize=16 width=0.5 shape=circle style=filled]"); out.newLine();
		    }
		    for(int i=0;i<EstadosFinais.size();i++) {
		    	out.write(EstadosFinais.get(i)+"[margin=0 color=cornflowerblue fontcolor=white fontsize=16 width=0.5 shape=doublecircle style=filled]"); out.newLine();
		    }
		    for(int i=0;i<Estados.size();i++) {
		    	if(!EstadosFinais.contains(Estados.get(i))) {
		    		if(!EstadosIniciais.contains(Estados.get(i))) {
		    			out.write(Estados.get(i)+"[margin=0 color=cornflowerblue fontcolor=white fontsize=16 width=0.5 shape=circle style=filled]");out.newLine();
		    		}
		    	}	    		 
		    }
		    if(EstadosFinais.contains(Estado)) {//se é final ou inicial
		    	out.write(Estado+"[margin=0 color=red fontcolor=white fontsize=16 width=0.5 shape=doublecircle style=filled]");out.newLine();
		    } else {
		    	out.write(Estado+"[margin=0 color=red fontcolor=white fontsize=16 width=0.5 shape=circle style=filled]");out.newLine();
		    }
		    
		    out.write("}");
		    out.newLine();
		    for(int i=0;i<EstadosIniciais.size();i++) {
		    	 out.write("start -> "+EstadosIniciais.get(i));out.newLine();
		    }
		    for(int i=0;i<ImprimirDot.size();i++) {
		    	if(dot.equals(ImprimirDot.get(i))) {
		    		dot = Estado+" -> "+Destino+" [ label="+c+" color=green];";
		    		out.write(dot);
			    	out.newLine();
		    	}
		    	else {
			    	out.write(ImprimirDot.get(i));
			    	out.newLine();
		    	}
		    }
	    
		    out.write("start [shape=Mdiamond];");
		    out.newLine();
		    out.write("}");
		    
		 }
			
		//System.out.println("dot -Tpng g"+x+".dot -o grafo"+x+".png");
		Process proc = Runtime.getRuntime().exec("dot -Tpng g"+x+".dot -o grafo"+x+".png");
	}
	
	private void criarDOTTransicaoND(int x, String Estado, Character c, ArrayList<String> Destino) throws FileNotFoundException, IOException {	//dot para transicoes não deterministicas
		String s="";String dot="";
		try(BufferedWriter out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream("g"+x+".dot"))))
		{
		    out.write("digraph {"); out.newLine();
		    out.write("{"); out.newLine();
		    for(int i=0;i<EstadosIniciais.size();i++) {
		    	out.write(EstadosIniciais.get(i)+"[margin=0 color=cornflowerblue fontcolor=white fontsize=16 width=0.5 shape=circle style=filled]"); out.newLine();
		    }
		    for(int i=0;i<EstadosFinais.size();i++) {
		    	out.write(EstadosFinais.get(i)+"[margin=0 color=cornflowerblue fontcolor=white fontsize=16 width=0.5 shape=doublecircle style=filled]"); out.newLine();
		    }
		    for(int i=0;i<Estados.size();i++) {
		    	if(!EstadosFinais.contains(Estados.get(i))) {
		    		if(!EstadosIniciais.contains(Estados.get(i))) {
		    			out.write(Estados.get(i)+"[margin=0 color=cornflowerblue fontcolor=white fontsize=16 width=0.5 shape=circle style=filled]");out.newLine();
		    		}
		    	}	    		 
		    }
		    if(EstadosFinais.contains(Estado)) {//se é final ou inicial
		    	out.write(Estado+"[margin=0 color=red fontcolor=white fontsize=16 width=0.5 shape=doublecircle style=filled]");out.newLine();
		    } else {
		    	out.write(Estado+"[margin=0 color=red fontcolor=white fontsize=16 width=0.5 shape=circle style=filled]");out.newLine();
		    }
		    
		    out.write("}");
		    out.newLine();
		    for(int i=0;i<EstadosIniciais.size();i++) {
		    	 out.write("start -> "+EstadosIniciais.get(i));out.newLine();
		    }
		    for(int i=0;i<ImprimirDot.size();i++) {
		    	for(int b =0;b<Destino.size();b++) {
			    	dot = Estado+" -> "+Destino.get(b)+" [ label="+c+"];";
			    	if(dot.equals(ImprimirDot.get(i))) {
			    		String dot1 = Estado+" -> "+Destino.get(b)+" [ label="+c+" color=green];";
			    		out.write(dot1);
				    	out.newLine();
			    	}	
		    	}
		    	if(!dot.equals(ImprimirDot.get(i))) {
			    	out.write(ImprimirDot.get(i));
			    	out.newLine();
		    	}
		    
		    }
	    
		    out.write("start [shape=Mdiamond];");
		    out.newLine();
		    out.write("}");
		    
		 }
			
		//System.out.println("dot -Tpng g"+x+".dot -o grafo"+x+".png");
		Process proc = Runtime.getRuntime().exec("dot -Tpng g"+x+".dot -o grafo"+x+".png");
		
	}
	
	private void criarDOTFinal(String EstadoAtual) throws IOException{//dot -Tpng g.dot -o grafo1.png
		try(BufferedWriter out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream("gfinal.dot"))))
		{
		    out.write("digraph {"); out.newLine();
		    out.write("{"); out.newLine();
		    for(int i=0;i<EstadosIniciais.size();i++) {
		    	out.write(EstadosIniciais.get(i)+"[margin=0 color=cornflowerblue fontcolor=white fontsize=16 width=0.5 shape=circle style=filled]"); out.newLine();
		    }
		    for(int i=0;i<EstadosFinais.size();i++) {
		    	out.write(EstadosFinais.get(i)+"[margin=0 color=cornflowerblue fontcolor=white fontsize=16 width=0.5 shape=doublecircle style=filled]"); out.newLine();
		    }
		    for(int i=0;i<Estados.size();i++) {
		    	if(!EstadosFinais.contains(Estados.get(i))) {
		    		if(!EstadosIniciais.contains(Estados.get(i))) {
		    			out.write(Estados.get(i)+"[margin=0 color=cornflowerblue fontcolor=white fontsize=16 width=0.5 shape=circle style=filled]");out.newLine();
		    		}
		    	}	    		 
		    }
		    if(EstadosFinais.contains(EstadoAtual)) {//se é final ou inicial
		    	out.write(EstadoAtual+"[margin=0 color=green fontcolor=white fontsize=16 width=0.5 shape=doublecircle style=filled]");out.newLine();
		    } else {
		    	out.write(EstadoAtual+"[margin=0 color=orange fontcolor=white fontsize=16 width=0.5 shape=circle style=filled]");out.newLine();
		    }
		    out.write("}");
		    out.newLine();
		    for(int i=0;i<EstadosIniciais.size();i++) {
		    	 out.write("start -> "+EstadosIniciais.get(i));out.newLine();
		    }
		    for(int i=0;i<ImprimirDot.size();i++) {
		    	out.write(ImprimirDot.get(i));
		    	//System.out.println(ImprimirDot.get(i));
		    	out.newLine();
		    }
	    
		    out.write("start [shape=Mdiamond];");
		    out.newLine();
		    out.write("}");
		    
		    Runtime.getRuntime().exec("dot -Tpng gfinal.dot -o grafofinal.png");
		 }
	}
	
	
//----------------------------------------------------------- AFD ---------------------------------------------------------------------------
	public void AFD(String[] linha1, ArrayList<String> transicao1, String palavra) {//testes de afds
		
		this.Estados = new ArrayList<String>();
		this.EstadosIniciais = new ArrayList<String>();
		this.EstadosFinais = new ArrayList<String>();
		Stack<Character> word = retornaPilha(palavra);
		String t;String[] t1;
		
		String i = linha1[0];String f = linha1[1];
		String[] iniciais = i.split(" "); String[] finais = f.split(" ");
		//Estados
		for(int x=0;x<iniciais.length; x++) {//Monta estados iniciais
			EstadosIniciais.add(iniciais[x]);
			Estados.add(iniciais[x]);
		}
		for(int x=1;x<finais.length; x++) {//Monta estados finais
			EstadosFinais.add(finais[x]);
			Estados.add(finais[x]);
		}
		for(int x=0;x<transicao1.size();x++) {
			t = transicao1.get(x);
			t = t.replace(">", "");t = t.replace("  ", " ");
			t1 = t.split(" ");
			if (!(Estados.contains(t1[0]))) {
				Estados.add(t1[0]);
			}
			if(!(Estados.contains(t1[2]))) {
				Estados.add(t1[2]);
			}
		}
		
		montarTransicao(transicao1);
		this.EstadoAtual = EstadosIniciais.get(0);
		System.out.println("|--- Estado Inicial: " +EstadoAtual +" ---|");
		
		if(EstadosIniciais.size() == 1) {
			executar(word); 
		} else {
			
		}
		
		System.out.println("|--- Leu toda a palavra ---|");
		System.out.println("|---    Estado: (" +EstadoAtual +")    ---|");
		if(word.isEmpty() && EstadosFinais.contains(EstadoAtual)){
			System.out.println("|---   Palavra Aceita   ---|");
		} else {
			System.out.println("|--- Palavra Rejeitada ---|");
		}
		
	}
	
	public void montarTransicao(ArrayList<String> transicao1) {//Monta transicoes
		this.Transicoes = new HashMap<String, String>();
		String t;String[] t1;String s;
		for(int x=0;x<transicao1.size();x++) {
			t = transicao1.get(x);
			t = t.replace(">", "");t = t.replace("  ", " ");
			t1 = t.split(" ");
			s = t1[0]+":"+t1[1];
			Transicoes.put(s, t1[2]);

		}
	}
	
	public void executar(Stack<Character> word) {//executa
		Character c;String transicao;
		if(!word.isEmpty()) {
				c = word.pop();
				transicao = EstadoAtual+":"+c;//key para buscar destino
				printTransicao(EstadoAtual,c,Transicoes.get(transicao));
				EstadoAtual = Transicoes.get(transicao);
				System.out.println("-------------------------------------------------------");
				executar(word);
		}
			
	}
	
	public Stack<Character> retornaPilha(String palavra) {//transforma a palavra em pilha
		Stack<Character> word = new Stack<>();
		char[] ch = palavra.toCharArray();
        for (int i = 0; i < palavra.length(); i++) {
        	word.push(ch[i]);
        }
        for(int i = 0; i < palavra.length(); i++) {
        	ch[i] = word.pop();
        }
        palavra = String.valueOf(ch);
        for (int i = 0; i < palavra.length(); i++) {
        	word.push(ch[i]);
        }
		return word;	
	}
	
	public void printTransicao(String e1, Character c, String e2) {//imprime cada transicao
		System.out.println("{ ESTADO ATUAL: " + e1 + " | CHAR LIDO: " + c + " | DESTINO-> " + e2 +" }");
	}
	
	
}

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

public class Automato {
	public ArrayList<String> Estados;
	public ArrayList<String> EstadosIniciais;
	public ArrayList<String> EstadosFinais;
	public Map<String, String> Transicoes;
	public Map<String, List<String>> AFN;   
	public ArrayList<String> TLambda;
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
			if(!(Alfabeto.contains(t1[1])) && !t1[1].contentEquals("/")) {
				Alfabeto.add(t1[1]);
			}
		}
		
		montarAFN(transicao1);
		
		if(TLambda.size()>0) {
			System.out.println("|----- Convertendo em AFN  -----|");
			converterAFN();
		}
		
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
			criarDOT(palavra);
			criarDOTFinal(EstadoAtual);
			System.out.println("|-------   DOT  Criado    -------|");
			Thread.sleep(3000);
			Runtime.getRuntime().exec("magick convert -delay 150 -loop 0 *.png automato.gif");	// converte para gif
			Thread.sleep(3000);
			System.out.println("|-----   Animação Criada    -----|");
		} catch (IOException | InterruptedException e) {
			
			e.printStackTrace();
		}
			
	}

	public void montarAFN(ArrayList<String> transicao1) {//Monta map de transicoes
		this.AFN = new HashMap<String, List<String>>();this.TLambda = new ArrayList<String>();
		String t;String[] t1;String s;String dot;
		for(int x=0;x<transicao1.size();x++) {//chave para cada transicao no formato -> "[ESTADO]:CHAR"
			t = transicao1.get(x);
			t = t.replace(">", "");t = t.replace("  ", " ");//remove os caracteres desnecessarios
			t1 = t.split(" ");		// splita em vetor [origem,char,destino]
			if(t1[1].contentEquals("/")) {//se lambda
				s = t1[0]+"-"+t1[2];	//Origem - Destino em Transicao Lambda
				TLambda.add(s);		//Adiciona a uma list
			} else {
				s = "["+t1[0]+"]:"+t1[1];	//[ESTADO]:CHAR"
				AFN.putIfAbsent(s, new ArrayList<String>());
				AFN.get(s).add(t1[2]);		//associa destino a [ESTADO]:VALOR/CHAR
				dot = t1[0]+" -> "+t1[2]+" [ label="+t1[1]+"];";//adiciona dot da transmicao
				this.ImprimirDot.add(dot);		
			}
		}	
	}
	
	public void converterAFN() {	//Converte AFN-Lambda em AFN
		String[] Lambda;
		for(int x=0;x<TLambda.size();x++) {//N de transicoes Lambda
			Lambda = TLambda.get(x).split("-");
			if(EstadosIniciais.contains(Lambda[0])) {//Se o Origem for Inicial
				EstadosIniciais.add(Lambda[1]);		 //O destino vira Inicial
			}
			for(int z=0; z<Alfabeto.size();z++) {		//Percorre Alfabeto e estados
				for(int y=0; y<Estados.size();y++) {	//Buscando transições que chegam na Origem
					try{
						for(int n=0;n<AFN.get("["+Estados.get(y)+"]:"+Alfabeto.get(z)).size();n++) {//Transicao Nao Deterministica
							String E = AFN.get("["+Estados.get(y)+"]:"+Alfabeto.get(z)).get(n);
							if(E.equals(Lambda[0])) {		//Se Chega na origem
								String s = "["+Estados.get(y)+"]:"+Alfabeto.get(z);
								AFN.putIfAbsent(s, new ArrayList<String>());	//Adiciona a transição para o destino
								AFN.get(s).add(Lambda[1]);
								String dot = Estados.get(y)+" -> "+Lambda[1]+" [ label="+Alfabeto.get(z)+"];";//adiciona dot da transicao
								this.ImprimirDot.add(dot);		
							}
						}
					} catch (NullPointerException e) {
						
					}
				}		
			}
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
				String transicao;	
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
						System.out.print("| Sem Caminho adiante | ");printTransicaoAFN2(EstadoAtualAFN.get(j), c, Destino);
					}
				}
				if (Destino.isEmpty()) { //nao achou nenhum destino possivel - rejeito
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
					String transicao;	
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
									e.printStackTrace();
								} catch (IOException e) {
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
	
	private void criarDOT(String palavra) throws IOException{//dot -Tpng g.dot -o grafo1.png
		try(BufferedWriter out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream("g.dot"))))
		{
		    out.write("digraph {"); out.newLine();
		    out.write("rankdir=LR;"); out.newLine();
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
	    
		    out.write("start [style=invis];");out.newLine();
		    out.write("label=\"Palavra = "+palavra+"\";");out.newLine();
		    out.write("labelloc=top;");out.newLine();
		    out.write("}");
		    
		    Runtime.getRuntime().exec("dot -Tpng g.dot -o grafo.png");
		 }
	}
	
	private void criarDOTTransicao(int x, String Estado, Character c, String Destino) throws IOException{//dot -Tpng g.dot -o grafo1.png
		String dot = Estado+" -> "+Destino+" [ label="+c+"];";
		try(BufferedWriter out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream("g"+x+".dot"))))
		{
		    out.write("digraph {"); out.newLine();
		    out.write("rankdir=LR;"); out.newLine();
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
	    
		    out.write("start [style=invis];");out.newLine();
		    out.write("label=\"Leu: "+c+" | "+Estado+" -> "+Destino+"\";");out.newLine();
		    out.write("labelloc=top;");out.newLine();
		    
		    out.write("}");
		    
		 }
			
		//System.out.println("dot -Tpng g"+x+".dot -o grafo"+x+".png");
		Runtime.getRuntime().exec("dot -Tpng g"+x+".dot -o grafo"+x+".png");
	}
	
	private void criarDOTTransicaoND(int x, String Estado, Character c, ArrayList<String> Destino) throws FileNotFoundException, IOException {	//dot para transicoes não deterministicas
		String dot="";
		try(BufferedWriter out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream("g"+x+".dot"))))
		{
		    out.write("digraph {"); out.newLine();
		    out.write("rankdir=LR;"); out.newLine();
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
			    		out.write(dot1);out.newLine();
			    	}	
		    	}
		    	if(!dot.equals(ImprimirDot.get(i))) {
			    	out.write(ImprimirDot.get(i));out.newLine();
		    	}
		    
		    }
	    
		    out.write("start [style=invis];");out.newLine();
		    out.write("label=\"Leu: "+c+" | "+Estado+" -> "+Destino+"\";");out.newLine();
		    out.write("labelloc=top;");out.newLine();
		    out.write("}");
		    
		 }
			
		Runtime.getRuntime().exec("dot -Tpng g"+x+".dot -o grafo"+x+".png");
		
	}
	
	private void criarDOTFinal(String EstadoAtual) throws IOException{//dot -Tpng g.dot -o grafo1.png
		try(BufferedWriter out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream("gfinal.dot"))))
		{
		    out.write("digraph {"); out.newLine();
		    out.write("rankdir=LR;"); out.newLine();
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
	    
		    out.write("start [style=invis];");out.newLine();
		    if(EstadosFinais.contains(EstadoAtual)) {//se é final
		    	out.write("label=\"Palavra Aceita\"");out.newLine();
			    out.write("labelloc=top;");out.newLine();
		    } else {
		    	out.write("label=\"Palavra Rejeitada\"");out.newLine();
			    out.write("labelloc=top;");out.newLine();
		    }
		    out.write("}");
		    
		    Runtime.getRuntime().exec("dot -Tpng gfinal.dot -o grafofinal.png");
		 }
	}
	
	
}

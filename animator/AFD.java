package animator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class AFD {
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
	//----------------------------------------------------------- AFD ---------------------------------------------------------------------------
		public void Automato(String[] linha1, ArrayList<String> transicao1, String palavra) {//testes de afds
			
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

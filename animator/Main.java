package animator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		Automato auto = new Automato();
		String texto="";String palavra;
		String linha = null;
		ArrayList<String> txt = new ArrayList<String>();
	
		//Inicio
		System.out.println("|--- Animador de AFDs ---|");
		System.out.println("|--- Gabriel Oliveira Silva 16.1.8091 ---|");
		
		while(true) {
			//System.out.println("|--- Insira o nome do arquivo ---|");
			//String arquivo = sc.nextLine();
			String arquivo = "afn2.txt";
			FileReader fr;
			try {
				fr = new FileReader(arquivo);
				BufferedReader br = new BufferedReader(fr);
				texto = br.readLine();
			    String linha1[] = texto.split(";");//estados iniciais e finais
				
				while((linha = br.readLine()) != null){//transicoes
					txt.add(linha);
				}
				
				palavra = txt.remove(txt.size()-1);//remove a palavra do final
				palavra = palavra.replace(" ","");palavra = palavra.replace("wrd","");palavra = palavra.replace(":","");

				System.out.println("_______________________________________________________");
				auto.AFN(linha1,txt,palavra);
				
				br.close();
				break;			
			} catch (IOException e) {
				System.out.println("|--- Arquivo Inválido ---|");
			}
		}
		sc.close();
	}

}

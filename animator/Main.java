package animator;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
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
		
		while(true) {
			//System.out.println("|--- Insira o nome do arquivo ---|");
			//String arquivo = sc.nextLine();
			String arquivo = "afn3.txt";
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
		
		System.out.println("|--- Deseja abrir a animação? 0-Não 1-Sim---|");
		String comando = sc.nextLine();
		switch(comando) {
			case "1":{
				//abrir o gif do automato
				try { 
					System.out.println("|-----      Abrindo...      -----|");
					File file = new File("automato.gif");   
					if(!Desktop.isDesktopSupported()){  
						return;  
					}  
					Desktop desktop = Desktop.getDesktop();  
					if(file.exists()) 
						desktop.open(file);  
				} catch(Exception e){  
					e.printStackTrace();  
				} 
			}
		}
		
		sc.close();
		System.out.println("|-----  Execução Encerrada  -----|");
	}

}

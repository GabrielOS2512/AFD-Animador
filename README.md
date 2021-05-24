# Animador de Autômatos
Programa que recebe um arquivo txt contendo as informações do autômato no formato descrito abaixo, retorna se a palavra é aceita e em sequencia produz um *gif* do autômato, mostrando suas transições ao ler a palavra.
## Avisos
Foi desenvolvido como um trabalho da disciplina de Teoria da Computação, e não almeja ser um projeto perfeito. Após cumprir os requisitos da entrega, decidi compartilhar para ajudar à outros alunos. Em caso de AFNs Lambda, o autômato retornado será um AFN padrão equivalente.

O programa retorna no *gif* o primeiro caminho percorrido que conseguir aceitar a palavra ou encaso de rejeição o último.
## Necessário para a execução
É necessário para a execução do programa :
* **GraphViz**: O programa gera arquivos *dot* para cada transição da palavra e é convertido pelo GraphViz em um arquivo imagem no formato *png*.
* **ImageMagick**: As imagens geradas são convertidas em um *gif*.

Ambos devem ser inseridos no PATH do sistema.

## Formato do Arquivo Inicial
O automato ser descrito em um arquivo txt que será lido e executado. O autômato deve ser descrito da seguinte forma:

Na primeira linha temos o estado inicial e final separado por **;**. Em caso de mais de um basta separar os estados por espaço.

**EstadoInicial ; EstadoFinal1 EstadoFinal2**

Nas linhas seguintes as transições devem ser descritas uma em cada linha e da seguinte forma:

**EstadoOrigem Caracter > EstadoDestino**

Após todas as transições forem descritas, a palavra a ser lida deve ser indicada da seguinte forma:

**wrd : palavra**

As linhas não podem estar vazias e os espaçamentos devem ser respeitados. O Lambda deve ser representado por um **/**

### Exemplo
Exemplo de um AFN com 2 estados iniciais e seu autômato resultante.

s0 s3 ; s2

s0 a > s0

s0 b > s2

s1 b > s1

s1 a > s2

s2 a > s2

s2 b > s1

s3 b > s3

s3 a > s1

wrd : aaa

![grafo](https://user-images.githubusercontent.com/48599711/119414386-def35400-bcc5-11eb-8534-d9b3c85bc084.png)

# Gerador de Senhas

Gerador de senhas fortes e aleatórias com interface gráfica, feito em Java puro (Swing), sem nenhuma dependência externa.

<p align="center">
  <img src="tela.png" alt="Janela do Gerador de Senhas" width="480">
</p>

## Funcionalidades

- **Tamanho ajustável**: de 4 a 64 caracteres, por um controle deslizante.
- **Tipos de caractere**: minúsculas, maiúsculas, números e símbolos, ligados ou desligados com um clique.
- **Indicador de força**: barra colorida com o nível da senha e a entropia em bits.
- **Copiar**: envia a senha para a área de transferência.
- **Geração instantânea**: qualquer mudança nas opções já gera uma senha nova.

## Como rodar

Você precisa do JDK 11 ou mais novo instalado. Para conferir, rode `java -version` no terminal.

```bash
git clone https://github.com/Dv-TiagoCarvalho/Gerador-de-Senha.git
cd Gerador-de-Senha
javac -encoding UTF-8 GeradorDeSenha.java
java GeradorDeSenha
```

## Como funciona

- **Aleatoriedade segura**: os caracteres são sorteados com `java.security.SecureRandom`, próprio para uso em segurança. O `java.util.Random` é previsível e não serve para senhas.
- **Um de cada tipo**: a senha sempre tem pelo menos um caractere de cada tipo escolhido. O restante é sorteado entre todos os tipos juntos.
- **Embaralhamento**: no final, os caracteres são embaralhados com o algoritmo de Fisher-Yates, para que os caracteres garantidos não fiquem sempre no começo.
- **Força da senha**: a entropia é calculada como `tamanho × log2(quantidade de caracteres possíveis)`.

| Entropia | Nível |
| --- | --- |
| menos de 40 bits | Fraca |
| 40 a 59 bits | Razoável |
| 60 a 79 bits | Forte |
| 80 bits ou mais | Muito forte |

## Estrutura

Todo o projeto está em um único arquivo, `GeradorDeSenha.java`, dividido em três partes:

1. **Lógica de geração**: os métodos `gerar` e `entropia`, que não dependem da interface.
2. **Estilo**: cores e fontes usadas na tela.
3. **Interface gráfica**: a janela e os componentes desenhados à mão (cartão da senha, barra de força, botões, opções e controle deslizante).

## Tecnologias

- Java
- Swing

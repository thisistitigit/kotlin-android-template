# Impostor

Aplicação Android local para jogos sociais, escrita em Kotlin, Room e Jetpack Compose. O fluxo visual implementado é `Splash → Onboarding → New Game → Players → Player names → Impostors → Content setup → Player turn → Content reveal → Everyone Ready → Voting → Results → Role reveal`; o backend guarda jogadores, sessões, rondas, papéis, conteúdo, votos e pontuação.

## Executar e validar

Usa o JBR 21 incluído no Android Studio para executar o Gradle e Android SDK 36. O bytecode da app continua configurado para Java 17. O projeto usa Kotlin 2.1, Room 2.7.2, KSP 2.1.0-1.0.29 e Paparazzi 2.0.0-alpha02; esta combinação mantém o compilador, Room, AGP e os previews alinhados. Em PowerShell:

```powershell
$env:JAVA_HOME = 'C:/Program Files/Android/Android Studio/jbr'
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :library-data:testDebugUnitTest
.\gradlew.bat detekt :app:lintDebug :library-compose:lintDebug :library-data:lintDebug
```

Para validar os previews sem emulador:

```powershell
$env:COMPOSE_PREVIEW_FILTER = 'PlayerNamePreview,PlayerTurnPreview'
.\gradlew.bat :library-compose:testDebugUnitTest --tests '*ComposePreviewer'
```

Os testes instrumentados precisam de emulador ou dispositivo:

```powershell
.\gradlew.bat :library-compose:connectedDebugAndroidTest
```

## Regras de jogo implementadas

Um `Game` é uma sessão com jogadores, sem quantidade de rondas pré-definida. Quando os jogadores escolhem jogar novamente, `StartRoundUseCase`/`StartConfiguredRoundUseCase` calcula automaticamente `MAX(round_number) + 1`. A nova ronda pode usar `CLASSIC`, `SIMILAR_WORD` ou `QUESTION`; a classificação continua a acumular na mesma sessão, mesmo quando o modo muda.

O Mr. White é opcional ao criar o jogo:

```kotlin
val gameId = backend.createGame(
    playerIds = playerIds,
    impostorCount = 1,
    mrWhiteCount = 2
)
```

Em Classic, cada Mr. White é escolhido entre jogadores que não são impostores e recebe sempre `contentId = null`, que representa a palavra vazia. O valor zero desativa este papel; outros modos não o atribuem. O cálculo legado de pontuação suporta `mrWhiteGuesserId`, mas o novo fluxo de votação sequencial termina por eliminação e não inclui um ecrã de adivinhação. A classificação é calculada a partir de `score_events`, a única fonte de verdade para pontos.

Inicializa o backend uma vez, idealmente num container de dependências da aplicação:

```kotlin
val backend = GameBackend.create(applicationContext)
backend.prepare()
```

`GameBackend` é o composition root atual. Expõe repositórios e os casos de uso `createGame`, `startRound`, `startConfiguredRound`, `submitAnswer`, `submitVote`, `finishRound`, `finishGame` e `getClassification`. Quando o projeto crescer, esta classe pode ser substituída por Hilt/Koin sem mudar as regras de domínio.

## Organização do código

```text
library-data/
  data/local/       Room database, converters, DAOs, entities e relations
  data/repository/  Transações e acesso aos dados
  domain/enums/     Estados, modos, papéis e motivos de pontuação
  domain/model/     Modelos usados pelas regras
  domain/usecase/   Operações de negócio
library-compose/
  ui/components/    Botões, cards, seletores, banner e círculos de modo
  ui/screens/       Splash, onboarding, home e configuração do jogo
  ui/theme/         Paleta, tipografia, formas e espaçamentos
```

As regras puras ficam em casos de uso e têm testes unitários. Operações que alteram várias tabelas ficam em transações no repositório. Os composables recebem estado e callbacks; não acedem diretamente ao Room.

## Paleta global

As cores ficam apenas em `library-compose/src/main/java/com/impostor/app/ui/theme/Color.kt`:

| Token | Hex | Uso |
| --- | --- | --- |
| `MainPurple` | `#897CFF` | cards e ações principais |
| `LightPurple` | `#D0CBFF` | texto secundário e indicadores |
| `AppBackground` | `#222222` | fundo geral |
| `MarkerYellow` | `#E2F163` | botões de progressão e marcadores |
| `DeepPurple` | `#292238` | cards de privacidade e progresso |
| `AppWhite` | `#FFFFFF` | texto/contornos |
| `AppBlack` | `#000000` | overlay e contraste |

Estes valores foram aproximados a partir das referências enviadas, porque os hexadecimais não vieram escritos na mensagem. Basta mudar os tokens para atualizar toda a app.

## Assets: nomes e caminhos

Os ficheiros raster usados diretamente pela aplicação ficam em:

```text
library-compose/src/main/res/drawable-nodpi/
  logo.png               Splash sem fundo
  welcoming_logo.png    Primeiro ecrã do onboarding
  text_logo.png          Cabeçalho do New Game
  vibrent_1.png … vibrent_27.png  Avatares randomizados
  cat_turn_01.png, cat_turn_02.png  Gatos randomizados do turno
design-assets/avatars/              Originais dos 27 avatares
```

O `logo.png` incluído foi extraído da referência com fundo transparente. `welcoming_logo.png` e `text_logo.png` são variantes de exemplo geradas para as proporções dos respetivos ecrãs; substitui-as pelos exports oficiais mantendo os nomes.

Os fundos estão hoje como gradientes compiláveis. Para usar as fotografias finais, remove o XML com o mesmo nome e adiciona a imagem a `drawable-nodpi`:

```text
Ecrã Welcome: library-compose/src/main/res/drawable-nodpi/onboarding_welcome.jpg
Ecrã Gather:  library-compose/src/main/res/drawable-nodpi/onboarding_gather.jpg
Ecrã Choose:  library-compose/src/main/res/drawable-nodpi/onboarding_choose.jpg
Ecrã Fun:     library-compose/src/main/res/drawable-nodpi/onboarding_fun.jpg
```

Os placeholders atuais estão em `library-compose/src/main/res/drawable/onboarding_*.xml`. O Kotlin usa apenas o nome do resource, portanto a troca de XML por JPG/PNG não exige alterações ao código. Todos os fundos recebem um overlay `#000000` a 50% em `OnboardingScreen`.

Outros assets:

```text
design-assets/home/classic.svg       SVG-fonte do modo Classic
design-assets/home/classic_not_selected.svg
design-assets/home/questions.svg     SVG-fonte do modo Questions
design-assets/home/questions_not_selected.svg
library-compose/src/main/res/drawable/classic.xml
library-compose/src/main/res/drawable/classic_not_selected.xml
library-compose/src/main/res/drawable/questions.xml
library-compose/src/main/res/drawable/questions_not_selected.xml
library-compose/src/main/res/drawable/mask_minimal.xml
```

Android não consome SVG diretamente em `res/drawable`; mantém os SVG em `design-assets` e converte/exporta para Vector Drawable XML com o Android Studio. Para substituir `mask.minimal.png`, usa o nome Android válido `mask_minimal.png` em `drawable-nodpi` e remove primeiro `mask_minimal.xml`.

Não coloques imagens em `mipmap`, exceto ícones de launcher. Fotografias grandes devem usar `drawable-nodpi` para evitar cópias redimensionadas no APK; ícones vetoriais devem ficar em `drawable`.

## Componentes reutilizáveis

`AppButton` é a base de todos os botões. `AppButtonStyle.PRIMARY`, `SECONDARY`, `FROSTED` e `OUTLINED` mudam o tratamento visual; `SECONDARY` aplica `MarkerYellow` e `showShadow` controla a sombra:

```kotlin
AppButton(
    text = "Next",
    onClick = onNext,
    style = AppButtonStyle.FROSTED,
    showShadow = true
)
```

`AppCard` recebe cor de fundo, contorno e qualquer conteúdo. O card roxo do onboarding é uma utilização deste componente. `GameModeCircle` recebe os SVGs convertidos de estado selecionado/não selecionado, `label`, `selected` e `onClick`; usa `LightPurple` no modo selecionado e mantém sempre o contorno branco.

`NumberSelector` é o sistema horizontal reutilizado nos ecrãs de jogadores, impostores e Mr. Whites. Recebe um `IntRange`, o valor selecionado e um callback; o pager aplica snapping e mantém o item central como seleção. `RoleSelector` mostra apenas Impostor em Questions e permite alternar entre Mr. White e Impostor em Classic.

`PlayerAvatar` apresenta um avatar de resource ou a fotografia capturada. Os 27 avatares são baralhados sem repetição ao configurar jogadores. `PlayerNameRoute` contém `TakePicturePreview`, enquanto `PlayerNameScreen` é uma UI pura; assim a câmara funciona no dispositivo e os previews não dependem de uma Activity. `PlayerTurnScreen` reutiliza `PlayerAvatar`, `AppCard` e `AppButton`, recebe a lista de jogadores que já viram o conteúdo e escolhe um gato do array `TURN_CAT_IMAGES`.

O guia para implementar palavra, pergunta e Mr. White está em [docs/NEXT_GAME_SCREENS.md](docs/NEXT_GAME_SCREENS.md), junto das referências em `docs/mockups/`.

## Tipografia Poppins

As fontes locais e a licença OFL estão em:

```text
library-compose/src/main/res/font/poppins_bold.ttf
library-compose/src/main/res/font/poppins_black.ttf
design-assets/fonts/OFL-Poppins.txt
```

Os estilos centralizados em `ui/theme/Typography.kt` são `title` (Poppins Black 35sp), `labelsScroll` (Bold 20sp), `scrollNumber` (Bold 40sp) e `chosenNumber` (Bold 64sp). Usa estes tokens em vez de criar `TextStyle` diretamente nos ecrãs.

Ao criar um componente novo:

1. Mantém o estado no ecrã/route e passa apenas valores e callbacks.
2. Aceita `modifier: Modifier = Modifier` e aplica-o ao nó exterior.
3. Usa `MaterialTheme`, os tokens de `Color.kt` e `Spacing`; evita hexadecimais/dimensões dispersos.
4. Coloca texto em `res/values/strings.xml` e descrições acessíveis em imagens informativas.
5. Adiciona preview e testa comportamento observável.

## Decisões e próximos pontos de integração

O botão `Continue` no New Game só fica ativo após escolher um modo. Players permite escolher entre 3 e 10 jogadores; o ecrã seguinte limita adversários para preservar pelo menos um civil. Classic permite quantidades independentes de Impostor e Mr. White, enquanto Questions fixa o seletor em Impostor e mantém `mrWhiteCount = 0`. O onboarding ainda é mostrado em cada arranque; guarda a conclusão em DataStore quando quiseres mostrá-lo apenas na primeira utilização.

O schema Room é exportado para `library-data/schemas`. A base está na versão 2, com migração v1→v2 testada que conserva os votos anteriores. Qualquer alteração futura ao schema deve incrementar a versão e incluir uma migration testada.

O [README original do template](README.upstream.md) continua disponível para referência.

## Votação

O percurso integrado é preparação de conteúdo real → passagem/revelação individual → Everyone Ready →
Vote → seleção → Vote Locked → resultados → revelação do papel. `GameFlowViewModel` mantém o estado
durante mudanças de configuração; os composables de votação recebem apenas estado e callbacks.

Adicionar manualmente, sem alterar os nomes:

| Ecrã | Ficheiro |
| --- | --- |
| Vote, it’s your turn | `library-compose/src/main/res/drawable-nodpi/pointing_cat.png` |
| Vote Locked | `library-compose/src/main/res/drawable-nodpi/locked_cat.png` |

Os PNGs não foram criados. Enquanto não existirem, surge um placeholder textual com o nome do ficheiro
e espaço reservado. Depois de adicionar os PNGs, reconstruir a app. Não há imagens alternativas.

`VotingDestination` representa o destino; `VotingSnapshot` representa as regras e transições persistidas.
`SubmitVoteUseCase` recebe agora `roundId`, `phaseId`, `voterId` e `votedPlayerId`. Cada fase congela os IDs
dos votantes/candidatos. Estes conjuntos são listas de IDs serializadas na entidade da fase, validadas
transacionalmente contra as atribuições da ronda, sem usar posições da UI como identidade.
O índice único `(phase_id, voter_game_player_id)` impede votos duplicados.
O esquema Room v2 inclui migração v1→v2 que conserva os votos antigos.

Todos os jogadores ativos votam pela ordem dos lugares, sem votar em si próprios. A confirmação bloqueia
o voto e exige passagem do telemóvel; só depois do último Got it aparecem resultados. Empates criam
outra fase com o mesmo eleitorado e apenas os candidatos empatados; empates repetidos continuam sem
eliminação automática. O Back em Vote Locked confirma a passagem, nunca reabre o voto.
O Back na seleção regressa à introdução e descarta a seleção ainda não submetida.

Apenas Reveal Role elimina o candidato mais votado. Eliminados deixam de votar e de ser candidatos.
Procuram-se os Impostors primeiro, depois os Mr. Whites restantes; Questions não admite Mr. White.
A ronda termina quando todos os adversários foram eliminados ou não restam civis ativos. As fases
resolvidas atribuem 2 pontos por voto correto e 3 pontos aos vencedores da ronda; os votos de fases
empatadas ficam no histórico e não atribuem pontos. A revelação, eliminação e pontuação final são atómicas.
Este fluxo de votação não acrescenta uma tentativa de adivinhação da palavra por Mr. White.

Para reutilizar a UI, usar `PlayerChip` para identificação, `PrivacyCard(text)` para avisos privados,
`VoteCandidateCard` para seleção e `VoterAvatars` para listas acessíveis sem cortes. `VotingFrame` centraliza
header, insets, largura máxima e scroll quando necessário. Novas mensagens pertencem a `strings.xml`;
novas cores e estilos pertencem a `Color.kt` e `Typography.kt`. Os previews de votação são independentes
de Activity, ViewModel e Room e incluem 320×480, oito candidatos e empate.


### Revelação e fim da ronda

O fluxo usa `RoleRevealScreen` (Civilian, Impostor e Mr. White), `MrWhiteGuessScreen`,
`RoundContentRevealScreen` (palavra ou pergunta), `RoundSummaryScreen` e `ScoresScreen`.
Os ecrãs recebem estado imutável e callbacks; as transições e a pontuação ficam no domínio/ViewModel.
O cartão `SecretContentCard` e o layout `GameScreenLayout` são partilhados com os ecrãs existentes.

Assets PNG adicionados manualmente, sem imagens geradas:

| Ecrã | Caminho |
| --- | --- |
| Civilian / The Word Was | `library-compose/src/main/res/drawable-nodpi/civilian_reveal_cat.png` |
| Guess The Word | `library-compose/src/main/res/drawable-nodpi/guess_word_cat.png` |
| Impostor descoberto | `library-compose/src/main/res/drawable/impostor.png` (existente) |
| Mr. White descoberto | `library-compose/src/main/res/drawable/mr_white.png` (existente) |
| The Question Was / Game Over | `library-compose/src/main/res/drawable/win.png` (existente) |

Os dois novos assets apresentam um placeholder com o nome enquanto não forem adicionados.
Não adicionar outro ficheiro com o mesmo nome em `drawable` e `drawable-nodpi`.

Mr. White tem uma única tentativa privada por eliminação. A comparação normaliza espaços,
Unicode e maiúsculas/minúsculas, mantendo acentos significativos. A migração Room 2→3
preserva o histórico e guarda a tentativa na respetiva fase. Não é possível continuar
ou pontuar enquanto a tentativa estiver pendente. Acertar termina a ronda com vitória
individual de Mr. White; falhar permite continuar se ainda existirem Civilians e adversários.
Só após o fim da ronda é revelado o conteúdo público.

`New Game` prepara outra ronda da mesma sessão e mantém a classificação, podendo mudar
entre Classic e Questions na preparação. Questions nunca atribui Mr. White. A pontuação
apresentada vem do domínio: +2 por voto correto, +3 por vitória e +7 pela vitória individual
de Mr. White. `View Scores` apresenta o total acumulado da sessão.


### Layout comum e seleção de categorias

Todos os ecrãs do jogo após Home usam `GameScreenLayout`: header comum, área de conteúdo
adaptativa e ação principal centrada, com margem inferior de `Spacing.extraLarge` (32dp)
a partir da zona segura do dispositivo. O teclado é tratado com `imePadding` nos ecrãs
com input. O botão não se desloca quando o conteúdo cresce; apenas o conteúdo faz scroll.
A ação secundária do resumo (View Scores) fica acima da ação principal, mantendo a sua posição.
`GameActionButton` reutiliza AppButton, com largura de 172dp e estilo configurável.

Para acrescentar um ecrã:

```kotlin
GameScreenLayout(
    onBack = onBack,
    bottomAction = { GameActionButton(actionLabel, onContinue) }
) {
    // Conteúdo; não acrescentar outro header, botão final ou Spacer inferior.
}
```

Para uma grelha/pager que já faz scroll, usar `scrollContent = false` e `Modifier.weight(1f)`
na área central. Para conteúdo longo, manter o scroll do layout. Usar a mesma margem inferior
em todos os ecrãs; estilos diferentes não devem alterar o posicionamento da ação principal.

Questions apresenta `CategoryScreen` antes da preparação do conteúdo, também em novas rondas
na mesma sessão. O carrossel suporta gestos e toque, destaca o cartão central e reutiliza
`PageIndicator` com onboarding. A categoria escolhida é guardada como chave estável no campo
`category` de `ContentSetEntity`; Classic continua sem categoria. O ZIP só contém ilustrações:
a preparação das perguntas existentes é mantida, sem inventar um banco de perguntas.

| Categoria | SVG original importado do ZIP |
| --- | --- |
| Atores | `library-compose/src/main/res/raw/category_actors.svg` |
| Filmes | `library-compose/src/main/res/raw/category_movies.svg` |
| NBA | `library-compose/src/main/res/raw/category_nba.svg` |
| Futebol | `library-compose/src/main/res/raw/category_football.svg` |
| Séries | `library-compose/src/main/res/raw/category_series.svg` |
| Cantores | `library-compose/src/main/res/raw/category_singers.svg` |

Android não suporta SVG diretamente em `res/drawable`. Estes ficheiros excedem os limites
de tamanho de paths compilados de VectorDrawable. Por isso os SVGs originais são preservados
em `res/raw` e desenhados com `rememberSvgPainter`/`CategoryCard`, sem conversão para PNG ou
bibliotecas adicionais. O adaptador suporta o formato destes assets: viewBox com origem 0,0,
paths, fill-rule, fill, stroke, stroke-width e stroke-linejoin. Novos SVGs com grupos,
transforms, gradientes, texto ou filtros precisam de normalização antes de serem usados.

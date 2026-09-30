# Impostor

Aplicação Android local para jogos sociais, escrita em Kotlin, Room e Jetpack Compose. O fluxo visual implementado é `Splash → Onboarding → New Game`; o backend guarda jogadores, sessões, rondas, papéis, conteúdo, votos e pontuação.

## Executar e validar

Usa JDK 17 e Android SDK 36. O projeto usa Room 2.8.5 com KSP 2.3.12 para compatibilidade com Kotlin 2.3. Em PowerShell:

```powershell
$env:JAVA_HOME = 'C:/Program Files/Java/jdk-17'
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :library-data:testDebugUnitTest
.\gradlew.bat detekt :app:lintDebug :library-compose:lintDebug :library-data:lintDebug
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
    includeMrWhite = true
)
```

Em cada ronda, o Mr. White é escolhido entre jogadores que não são impostores e recebe sempre `contentId = null`, que representa a palavra vazia. Se for eliminado e adivinhar a palavra, passa `mrWhiteGuessedSecret = true` ao fechar a ronda e recebe a vitória própria. A classificação é calculada a partir de `score_events`, a única fonte de verdade para pontos.

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
  ui/components/    AppButton, AppCard, SkipButton e GameModeCircle
  ui/screens/       SplashScreen, OnboardingScreen e HomeScreen
  ui/theme/         Paleta, tipografia, formas e espaçamentos
```

As regras puras ficam em casos de uso e têm testes unitários. Operações que alteram várias tabelas ficam em transações no repositório. Os composables recebem estado e callbacks; não acedem diretamente ao Room.

## Paleta global

As cores ficam apenas em `library-compose/src/main/java/com/ncorti/kotlin/template/app/ui/theme/Color.kt`:

| Token | Hex | Uso |
| --- | --- | --- |
| `MainPurple` | `#806FF5` | cards e ações principais |
| `LightPurple` | `#C8C1FF` | texto secundário e indicadores |
| `AppBackground` | `#222222` | fundo geral |
| `MarkerYellow` | `#E7FA55` | modo de jogo selecionado |
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
design-assets/home/questions.svg     SVG-fonte do modo Questions
library-compose/src/main/res/drawable/classic.xml
library-compose/src/main/res/drawable/questions.xml
library-compose/src/main/res/drawable/mask_minimal.xml
```

Android não consome SVG diretamente em `res/drawable`; mantém os SVG em `design-assets` e converte/exporta para Vector Drawable XML com o Android Studio. Para substituir `mask.minimal.png`, usa o nome Android válido `mask_minimal.png` em `drawable-nodpi` e remove primeiro `mask_minimal.xml`.

Não coloques imagens em `mipmap`, exceto ícones de launcher. Fotografias grandes devem usar `drawable-nodpi` para evitar cópias redimensionadas no APK; ícones vetoriais devem ficar em `drawable`.

## Componentes reutilizáveis

`AppButton` é a base de todos os botões. `AppButtonStyle.PRIMARY`, `FROSTED` e `OUTLINED` mudam o tratamento visual; `showShadow` controla a sombra:

```kotlin
AppButton(
    text = "Next",
    onClick = onNext,
    style = AppButtonStyle.FROSTED,
    showShadow = true
)
```

`AppCard` recebe cor de fundo, contorno e qualquer conteúdo. O card roxo do onboarding é uma utilização deste componente. `GameModeCircle` generaliza os círculos do New Game e recebe `iconRes`, `label`, `selected` e `onClick`; `MarkerYellow` só aparece no item selecionado.

Ao criar um componente novo:

1. Mantém o estado no ecrã/route e passa apenas valores e callbacks.
2. Aceita `modifier: Modifier = Modifier` e aplica-o ao nó exterior.
3. Usa `MaterialTheme`, os tokens de `Color.kt` e `Spacing`; evita hexadecimais/dimensões dispersos.
4. Coloca texto em `res/values/strings.xml` e descrições acessíveis em imagens informativas.
5. Adiciona preview e testa comportamento observável.

## Decisões e próximos pontos de integração

O botão `Continue` no New Game já valida visualmente a seleção do modo. O callback está pronto para abrir a configuração de jogadores, impostores e Mr. White quando esse ecrã for criado. O onboarding ainda é mostrado em cada arranque; guarda a conclusão em DataStore quando quiseres mostrá-lo apenas na primeira utilização.

O schema Room é exportado para `library-data/schemas`. Como esta base ainda está na versão 1, as entidades foram normalizadas diretamente. Depois de publicares uma versão da app, qualquer alteração ao schema deve incrementar a versão e incluir uma migration testada.

O [README original do template](README.upstream.md) continua disponível para referência.

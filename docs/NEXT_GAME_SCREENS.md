# Guia dos ecrãs de revelação

Este guia continua o fluxo implementado em `PlayerTurnScreen`. A referência visual recebida está guardada em [content-reveal-reference.png](mockups/content-reveal-reference.png). Os gatos finais ainda não foram fornecidos individualmente; usa temporariamente `cat_reveal_placeholder.png` em `library-compose/src/main/res/drawable-nodpi/` e troca o ficheiro sem alterar o Kotlin.

## Arquitetura proposta

Cria um único `ContentRevealScreen` configurado pelo conteúdo. A route obtém o jogador/round do ViewModel e o composable recebe apenas estado imutável e callbacks:

```kotlin
sealed interface RevealContent {
    data class Word(val value: String) : RevealContent
    data class Question(val value: String) : RevealContent
    data object MrWhite : RevealContent
}

data class ContentRevealUiState(
    val player: TurnPlayerUi,
    val playerNumber: Int,
    val content: RevealContent,
    @DrawableRes val catRes: Int,
    val isVisible: Boolean = true
)

@Composable
fun ContentRevealScreen(
    state: ContentRevealUiState,
    onBack: () -> Unit,
    onHideContent: () -> Unit,
    onGotIt: () -> Unit,
    modifier: Modifier = Modifier
)
```

Não passes entidades Room para Compose. O ViewModel transforma domínio em `ContentRevealUiState`; o ecrã apresenta esse estado. Guarda o conteúdo sensível fora de `rememberSaveable`, para que uma palavra não seja escrita no saved state do sistema.

## Componentes comuns

- `PlayerChip`: reutiliza `PlayerAvatar` (32dp), mostra “Player N” e o nickname em `MainPurple`.
- `RevealHeading`: recebe o nome e o tipo; produz “Tiago Your Word Is”, “Tiago Your Question Is” ou “You’re Mr. White”.
- `ContentBubble`: para `Word`, card rodado cerca de -7°; para `Question`, balão com pequena cauda. Ambos usam `MainPurple`, `AppTextStyles.title` e texto centrado.
- `PrivacyCard`: reutiliza `AppCard` com `DeepPurple`, ícone de cadeado, e texto “Don’t show your word/question to other players”. Deve cobrir o conteúdo quando `isVisible == false`.
- `AppButton(style = AppButtonStyle.SECONDARY)`: botão “Got it!”.
- `PlayerTurnScreen`: continua responsável pela passagem física do telemóvel e pela lista de quem já viu o conteúdo.

Todos recebem `modifier` no nó exterior, textos por parâmetro ou `strings.xml`, e descrições de acessibilidade apenas quando a imagem acrescenta informação.

## Palavra — Classic

Segue a metade superior da referência: `PlayerChip`, título em duas linhas, subtítulo “Remember it”, `ContentBubble` retangular inclinado, gato placeholder, `PrivacyCard` e “Got it!”. O valor vem da associação `RoundPlayer.contentId`; nunca escolhas outra palavra dentro do composable.

Assets:

```text
library-compose/src/main/res/drawable-nodpi/cat_reveal_placeholder.png
library-compose/src/main/res/drawable/ic_lock.xml
```

Ao tocar “Got it!”, limpa o conteúdo visível, marca o jogador como visto no estado da ronda e navega para o próximo `PlayerTurnScreen`. No último jogador, navega para o início da ronda.

## Pergunta — Questions

Usa o mesmo scaffold. Troca apenas o título, o texto de privacidade e a variante `Question` de `ContentBubble`. O balão pode adaptar a altura ao texto e deve limitar a largura com padding horizontal; não fixes a pergunta numa imagem.

O botão do ecrã anterior já usa “See Question” quando `GameModeType.QUESTION`. O modo Questions não apresenta Mr. White e deve manter `mrWhiteCount = 0`.

## You’re Mr. White — Classic

Usa `RevealContent.MrWhite`. Mostra `PlayerChip`, “You’re Mr. White”, uma explicação curta de que o jogador não recebe palavra, o gato placeholder e o mesmo `PrivacyCard`. Não mostres string vazia nem um card “YOUR WORD”: no backend, Mr. White é representado por `contentId = null`, e a UI deve mapear esse estado explicitamente para `MrWhite`.

Este ecrã só pode aparecer em Classic e apenas quando `mrWhiteCount > 0`. “Got it!” segue exatamente o mesmo fluxo dos restantes jogadores.

## Previews e testes

Adiciona três previews puros: `WordRevealPreview`, `QuestionRevealPreview` e `MrWhiteRevealPreview`. Usa resources locais e callbacks vazios, sem ViewModel, Room, launcher de câmara ou navegação. Junta-os a `ComposePreviewer` para validar com Paparazzi.

Os testes de domínio devem confirmar o mapeamento `contentId == null && role == MR_WHITE -> RevealContent.MrWhite`. Na UI, um screenshot por variante é suficiente; testa comportamento apenas para ações que mudem estado ou navegação.

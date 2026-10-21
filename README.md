# MiraPro Android — projeto inicial

App Android nativo em Kotlin: interface escura, detecção básica do dispositivo, configuração por regras, opções de DPI, HUD de 2 a 5 dedos e histórico local SQLite.

## Incluído
- Marca, fabricante, modelo, Android, resolução, densidade lógica e taxa de atualização reportada quando a API permite.
- Sensibilidades não aleatórias para Geral, Ponto vermelho, Mira 2x, Mira 4x, AWM e Olhadinha.
- Ajustes iniciais por estilo de jogo, número de dedos e alguns dados de tela/Android.
- Sugestão de botão de atirar, gráficos e FPS.
- Histórico local e notas no SQLite. Sem conta, rede ou permissões sensíveis.

## Limitações honestas
Os números são heurísticas iniciais explicáveis, não valores medidos empiricamente em cada aparelho. Não é possível inferir uma sensibilidade perfeita apenas pelo nome do celular. Android não expõe a todos os apps o FPS real do Free Fire, a taxa de amostragem do toque ou todos os dados detalhados de RAM/CPU. O app não altera DPI, não mexe no jogo e não aplica HUD automaticamente. Use os valores como ponto de partida, teste no treinamento e mude apenas uma variável por vez.

## Abrir e compilar
1. Instale Android Studio e um JDK compatível com Android Gradle Plugin.
2. Extraia o ZIP e abra a pasta `MiraProAndroid` no Android Studio.
3. Aguarde a sincronização do Gradle e instale Android SDK 35 se solicitado.
4. Conecte um Android com depuração USB ou use um emulador e clique em **Run**.
5. Para APK: **Build > Build APK(s)**. APK de debug normalmente fica em `app/build/outputs/apk/debug/app-debug.apk`.

## Evoluções recomendadas
Adicionar um fluxo de calibração estruturado (passou da cabeça / ficou abaixo / tremor / acertou), ajustes por sessão, catálogo de dispositivos validado com fontes e datas, exportação de perfis e testes automatizados. O projeto ainda precisa ser compilado e testado no Android Studio e em aparelhos reais antes de distribuição.

## Compilação na nuvem pelo GitHub Actions (sem computador)
O projeto inclui `.github/workflows/build-apk.yml`. Depois de enviar os arquivos do projeto para um repositório GitHub, abra a aba **Actions**, execute **Build MiraPro APK** (ou aguarde a compilação automática após enviar para `main`/`master`) e baixe o artefato **MiraPro-debug-APK**. Dentro do artefato estará `app-debug.apk`. O GitHub pode exigir conta e que Actions esteja habilitado. Esse processo compila na nuvem; o APK ainda deve ser testado no aparelho antes de uso.

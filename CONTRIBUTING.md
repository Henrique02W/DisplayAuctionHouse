# Contribuindo

Obrigado por querer ajudar! 🏛️

## Ambiente

- **JDK 25** e Maven 3.9+
- Um servidor **Paper 26.2** com Vault e um plugin de economia para testes manuais

```
git clone https://github.com/Henrique02W/DisplayAuctionHouse.git
cd DisplayAuctionHouse
mvn verify
```

O JAR sai em `target/`. Copie-o para a pasta `plugins/` de um servidor de teste.

## Fluxo

1. Abra uma issue descrevendo o bug ou a ideia (ou comente numa existente).
2. Faça um fork e crie uma branch a partir de `main` (`fix/...`, `feat/...`).
3. Mantenha os commits pequenos e com mensagens claras (`fix: ...`, `feat: ...`, `docs: ...`).
4. Abra o Pull Request explicando o que mudou e como testou (versão do Paper incluída).

O CI precisa passar (`mvn verify` com JDK 25) antes do merge.

## Estilo

- Java 25, sem novas dependências sem discussão prévia.
- Textos exibidos ao jogador ficam em `messages` no `config.yml`, não no código.
- Novas opções de configuração devem ter valor padrão no `config.yml` e ser documentadas no README.
- Registre mudanças relevantes no `CHANGELOG.md`.

## Licença

Ao contribuir, você concorda que sua contribuição será distribuída sob a mesma
[Custom Non-Commercial Software License v1.0](LICENSE.md) do projeto.

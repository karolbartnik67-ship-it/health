# Heart Indicator 2.0 (Fabric 1.21.4, klient)

Przerobiony mod spunkyinsaan (licencja MIT w `LICENSE`).

- **Usunięte** serca nad głowami graczy.
- Na ekranie pokazuje serca gracza, **którego uderzyłeś** (przez **6 sekund**; kolejne trafienie odświeża czas, trafienie innego gracza przełącza na niego; po 6 s znika): np. `2/10` = 2 serca z 10 (max może być też np. 15). Połówki serca: `7.5/10`. Złote serca: `+2`.
- **GUI (klawisz J):** bez przycisków — łapiesz napis myszką i przesuwasz; kwadracik ze strzałką ↘ przy napisie ciągniesz w różne strony, żeby go powiększyć/pomniejszyć (kółko myszy też działa). Esc = gotowe.
- Klawisz **H** = szybkie włączenie/wyłączenie.

Build: GitHub → Actions → **HeartIndicator-mod**, albo `gradle build`.

# Podaci za MacroMenzu (NN 120/2013)

- namirnice.csv – nutritivne vrijednosti na 100 g (okvirne, USDA/EU tablice); grama_po_komadu za "(kom)" artikle.
- jela.csv – 311 jela iz NN 120/2013 (id, naziv, kategorija, masa sastojaka, cijena, napomena) + kcal_ocekivano za provjeru izračuna.
- jelo_sastojak.csv – 1600 veza jelo → namirnica s gramažom (neto; kod "ili" alternativa uzeta je prva opcija).
- meniji.csv / meni_stavke.csv – 84 menija i njihove stavke (jelo_id iz jela.csv, kolicina_porcija npr. 3 šnite kruha).

Provjera: SUM(namirnica.kcal_100g * grami / 100) po jelu mora odgovarati kcal_ocekivano (±1 zbog zaokruživanja).

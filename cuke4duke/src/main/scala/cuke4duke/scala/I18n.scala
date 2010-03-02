package cuke4duke.scala

trait AR { this: Dsl =>
  val بفرض = new Step("بفرض")
  val متى = new Step("متى")
  val عندما = new Step("عندما")
  val اذاً = new Step("اذاً")
  val ثم = new Step("ثم")
}

trait BG { this: Dsl =>
  val Дадено = new Step("Дадено")
  val Когато = new Step("Когато")
  val То = new Step("То")
}

trait CAT { this: Dsl =>
  val Donat = new Step("Donat")
  val Donada = new Step("Donada")
  val Atès = new Step("Atès")
  val Atesa = new Step("Atesa")
  val Quan = new Step("Quan")
  val Aleshores = new Step("Aleshores")
  val Cal = new Step("Cal")
}

trait CS { this: Dsl =>
  val Pokud = new Step("Pokud")
  val Když = new Step("Když")
  val Pak = new Step("Pak")
}

trait CY { this: Dsl =>
  val anrhegediga = new Step("anrhegediga")
  val Pryd = new Step("Pryd")
  val Yna = new Step("Yna")
}

trait DA { this: Dsl =>
  val Givet = new Step("Givet")
  val Når = new Step("Når")
  val Så = new Step("Så")
}

trait DE { this: Dsl =>
  val Angenommen = new Step("Angenommen")
  val Gegebensei = new Step("Gegebensei")
  val Wenn = new Step("Wenn")
  val Dann = new Step("Dann")
}

trait EN { this: Dsl =>
  val Given = new Step("Given")
  val When = new Step("When")
  val Then = new Step("Then")
}

trait ENAU { this: Dsl =>
  val Yaknowhow = new Step("Yaknowhow")
  val When = new Step("When")
  val Yagotta = new Step("Yagotta")
}

trait ENLOL { this: Dsl =>
  val ICANHAZ = new Step("ICANHAZ")
  val WEN = new Step("WEN")
  val DEN = new Step("DEN")
}

trait ENTX { this: Dsl =>
  val Givenyall = new Step("Givenyall")
  val Whenyall = new Step("Whenyall")
  val Thenyall = new Step("Thenyall")
}

trait ES { this: Dsl =>
  val Dado = new Step("Dado")
  val Cuando = new Step("Cuando")
  val Entonces = new Step("Entonces")
}

trait ET { this: Dsl =>
  val Eeldades = new Step("Eeldades")
  val Kui = new Step("Kui")
  val Siis = new Step("Siis")
}

trait FI { this: Dsl =>
  val Oletetaan = new Step("Oletetaan")
  val Kun = new Step("Kun")
  val Niin = new Step("Niin")
}

trait FR { this: Dsl =>
  val Soit = new Step("Soit")
  val Etantdonné = new Step("Etantdonné")
  val Quand = new Step("Quand")
  val Lorsque = new Step("Lorsque")
  val Lorsqu = new Step("Lorsqu")
  val Alors = new Step("Alors")
}

trait HE { this: Dsl =>
  val בהינתן = new Step("בהינתן")
  val כאשר = new Step("כאשר")
  val אז = new Step("אז")
  val אזי = new Step("אזי")
}

trait HR { this: Dsl =>
  val Zadan = new Step("Zadan")
  val Zadani = new Step("Zadani")
  val Zadano = new Step("Zadano")
  val Kada = new Step("Kada")
  val Kad = new Step("Kad")
  val Onda = new Step("Onda")
}

trait HU { this: Dsl =>
  val Ha = new Step("Ha")
  val Majd = new Step("Majd")
  val Akkor = new Step("Akkor")
}

trait ID { this: Dsl =>
  val Dengan = new Step("Dengan")
  val Ketika = new Step("Ketika")
  val Maka = new Step("Maka")
}

trait IT { this: Dsl =>
  val Dato = new Step("Dato")
  val Quando = new Step("Quando")
  val Allora = new Step("Allora")
}

trait JA { this: Dsl =>
  val 前提 = new Step("前提")
  val もし = new Step("もし")
  val ならば = new Step("ならば")
}

trait KO { this: Dsl =>
  val 조건 = new Step("조건")
  val 먼저 = new Step("먼저")
  val 만일 = new Step("만일")
  val 만약 = new Step("만약")
  val 그러면 = new Step("그러면")
}

trait LT { this: Dsl =>
  val Duota = new Step("Duota")
  val Kai = new Step("Kai")
  val Tada = new Step("Tada")
}

trait LV { this: Dsl =>
  val Kad = new Step("Kad")
  val Ja = new Step("Ja")
  val Tad = new Step("Tad")
}

trait NL { this: Dsl =>
  val Gegeven = new Step("Gegeven")
  val Stel = new Step("Stel")
  val Als = new Step("Als")
  val Dan = new Step("Dan")
}

trait NO { this: Dsl =>
  val Gitt = new Step("Gitt")
  val Når = new Step("Når")
  val Så = new Step("Så")
}

trait PL { this: Dsl =>
  val Zakładając = new Step("Zakładając")
  val Jeżeli = new Step("Jeżeli")
  val Wtedy = new Step("Wtedy")
}

trait PT { this: Dsl =>
  val Dado = new Step("Dado")
  val Quando = new Step("Quando")
  val Então = new Step("Então")
  val Entao = new Step("Entao")
}

trait RO { this: Dsl =>
  val Daca = new Step("Daca")
  val Cand = new Step("Cand")
  val Atunci = new Step("Atunci")
}

trait RO2 { this: Dsl =>
  val Dacă = new Step("Dacă")
  val Când = new Step("Când")
  val Atunci = new Step("Atunci")
}

trait RU { this: Dsl =>
  val Допустим = new Step("Допустим")
  val Если = new Step("Если")
  val То = new Step("То")
}

trait SE { this: Dsl =>
  val Givet = new Step("Givet")
  val När = new Step("När")
  val Så = new Step("Så")
}

trait SK { this: Dsl =>
  val Pokiaľ = new Step("Pokiaľ")
  val Keď = new Step("Keď")
  val Tak = new Step("Tak")
}

trait SR { this: Dsl =>
  val Задато = new Step("Задато")
  val Задате = new Step("Задате")
  val Задати = new Step("Задати")
  val Када = new Step("Када")
  val Кад = new Step("Кад")
  val Онда = new Step("Онда")
}

trait SRLATN { this: Dsl =>
  val Zadato = new Step("Zadato")
  val Zadate = new Step("Zadate")
  val Zatati = new Step("Zatati")
  val Kada = new Step("Kada")
  val Kad = new Step("Kad")
  val Onda = new Step("Onda")
}

trait TR { this: Dsl =>
  val Diyelimki = new Step("Diyelimki")
  val Eğerki = new Step("Eğerki")
  val Ozaman = new Step("Ozaman")
}

trait UZ { this: Dsl =>
  val Агар = new Step("Агар")
  val Унда = new Step("Унда")
}

trait VI { this: Dsl =>
  val Biết = new Step("Biết")
  val Cho = new Step("Cho")
  val Khi = new Step("Khi")
  val Thì = new Step("Thì")
}

trait ZHCN { this: Dsl =>
  val 假如 = new Step("假如")
  val 当 = new Step("当")
  val 那么 = new Step("那么")
}

trait ZHTW { this: Dsl =>
  val 假設 = new Step("假設")
  val 當 = new Step("當")
  val 那麼 = new Step("那麼")
}


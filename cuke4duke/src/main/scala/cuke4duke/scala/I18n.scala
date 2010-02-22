package cuke4duke.scala

trait Italian { this: Dsl =>
  val Dato = new Step("Dato")
  val Quando = new Step("Quando")
  val Allora = new Step("Allora")
}
trait Welsh { this: Dsl =>
  val AnrhegedigA = new Step("AnrhegedigA")
  val Pryd = new Step("Pryd")
  val Yna = new Step("Yna")
}
trait Catalan { this: Dsl =>
  val Donat = new Step("Donat")
  val Quan = new Step("Quan")
  val Aleshores = new Step("Aleshores")
}
trait NO { this: Dsl =>
  val Gitt = new Step("Gitt")
  val Når = new Step("Når")
  val Så = new Step("Så")
}
trait Japanese { this: Dsl =>
  val 前提 = new Step("前提")
  val もし = new Step("もし")
  val ならば = new Step("ならば")
}
trait French { this: Dsl =>
  val Soit = new Step("Soit")
  val Lorsque = new Step("Lorsque")
  val Alors = new Step("Alors")
}
trait Texan { this: Dsl =>
  val GivenYall = new Step("GivenYall")
  val WhenYall = new Step("WhenYall")
  val ThenYall = new Step("ThenYall")
}
trait German { this: Dsl =>
  val GegebenSei = new Step("GegebenSei")
  val Wenn = new Step("Wenn")
  val Dann = new Step("Dann")
}
trait Czech { this: Dsl =>
  val Pokud = new Step("Pokud")
  val Když = new Step("Když")
  val Pak = new Step("Pak")
}
trait ChineseTraditional { this: Dsl =>
  val 假設 = new Step("假設")
  val 當 = new Step("當")
  val 那麼 = new Step("那麼")
}
trait Slovak { this: Dsl =>
  val Pokiaľ = new Step("Pokiaľ")
  val Keď = new Step("Keď")
  val Tak = new Step("Tak")
}
trait Hungarian { this: Dsl =>
  val Ha = new Step("Ha")
  val Majd = new Step("Majd")
  val Akkor = new Step("Akkor")
}
trait Russian { this: Dsl =>
  val Допустим = new Step("Допустим")
  val Если = new Step("Если")
  val То = new Step("То")
}
trait Finnish { this: Dsl =>
  val Oletetaan = new Step("Oletetaan")
  val Kun = new Step("Kun")
  val Niin = new Step("Niin")
}
trait Spanish { this: Dsl =>
  val Dado = new Step("Dado")
  val Cuando = new Step("Cuando")
  val Entonces = new Step("Entonces")
}
trait Portuguese { this: Dsl =>
  val Dado = new Step("Dado")
  val Quando = new Step("Quando")
  val Então = new Step("Então")
}
trait Korean { this: Dsl =>
  val 조건 = new Step("조건")
  val 만일 = new Step("만일")
  val 그러면 = new Step("그러면")
}
trait Estonian { this: Dsl =>
  val Eeldades = new Step("Eeldades")
  val Kui = new Step("Kui")
  val Siis = new Step("Siis")
}
trait Indonesian { this: Dsl =>
  val Dengan = new Step("Dengan")
  val Ketika = new Step("Ketika")
  val Maka = new Step("Maka")
}
trait Lolcat { this: Dsl =>
  val ICanHaz = new Step("ICanHaz")
  val Wen = new Step("Wen")
  val Den = new Step("Den")
}
trait Bulgarian { this: Dsl =>
  val Дадено = new Step("Дадено")
  val Когато = new Step("Когато")
  val То = new Step("То")
}
trait Polish { this: Dsl =>
  val Zakładając = new Step("Zakładając")
  val Jeżeli = new Step("Jeżeli")
  val Wtedy = new Step("Wtedy")
}
trait Australian { this: Dsl =>
  val YaKnowHow = new Step("YaKnowHow")
  val YaGotta = new Step("YaGotta")
}
trait Arabic { this: Dsl =>
  val بفرض = new Step("بفرض")
  val متى = new Step("متى")
  val اذاً = new Step("اذاً")
}
trait Swedish { this: Dsl =>
  val Givet = new Step("Givet")
  val När = new Step("När")
  val Så = new Step("Så")
}
trait Romanian { this: Dsl =>
  val Daca = new Step("Daca")
  val Cand = new Step("Cand")
  val Atunci = new Step("Atunci")
}
trait Hebrew { this: Dsl =>
  val בהינתן = new Step("בהינתן")
  val כאשר = new Step("כאשר")
  val אז = new Step("אז")
}
trait Danish { this: Dsl =>
  val Givet = new Step("Givet")
  val Når = new Step("Når")
  val Så = new Step("Så")
}
trait Vietnamese { this: Dsl =>
  val Biết = new Step("Biết")
  val Khi = new Step("Khi")
  val Thì = new Step("Thì")
}
trait Dutch { this: Dsl =>
  val Gegeven = new Step("Gegeven")
  val Als = new Step("Als")
  val Dan = new Step("Dan")
}
trait Lithuanian { this: Dsl =>
  val Duota = new Step("Duota")
  val Kai = new Step("Kai")
  val Tada = new Step("Tada")
}
trait EN { this: Dsl =>
  val Given = new Step("Given")
  val When = new Step("When")
  val Then = new Step("Then")
}
trait ChineseSimplified { this: Dsl =>
  val 假如 = new Step("假如")
  val 当 = new Step("当")
  val 那么 = new Step("那么")
}
trait RomanianDiacritical { this: Dsl =>
  val Dacă = new Step("Dacă")
  val Când = new Step("Când")
  val Atunci = new Step("Atunci")
}
trait Latvian { this: Dsl =>
  val Kad = new Step("Kad")
  val Ja = new Step("Ja")
  val Tad = new Step("Tad")
}
trait Croatian { this: Dsl =>
  val Zadan = new Step("Zadan")
  val Kada = new Step("Kada")
  val Onda = new Step("Onda")
}

package org.dialos.handyhelfer

/**
 * Die Fernhilfe-Programme, die HANDYHelfer kennt.
 *
 * **Warum eine Liste und nicht eines:** Am 2026-10-08 hat sich am lebenden
 * Geraet gezeigt, dass die Wahl nicht frei ist, sondern von Googles Regeln
 * bestimmt wird - und dass die naheliegende Wahl die schlechteste war.
 *
 * Die Regel, an der alles haengt: Android laesst den Bedienhilfe-Dienst, ohne
 * den niemand aus der Ferne tippen kann, **nur fuer Apps aus Google Play**
 * zu (bzw. der HUAWEI AppGallery). Alles andere bekommt den Schalter
 * ausgegraut, mit dem Hinweis "Gesteuert durch eingeschraenkte Einstellung" -
 * und auf manchen Geraeten, etwa dem Motorola edge 50 neo mit Android 16,
 * fehlt sogar der Menuepunkt, mit dem sich das aufheben liesse.
 *
 * Daraus folgt eine unangenehme Zwickmuehle, die keine App aufloest:
 * - *Ausserhalb* von Play sperrt Android die Steuerung.
 * - *Innerhalb* von Play muss der Anbieter Googles Genehmigungsverfahren
 *   durchlaufen - mit Formular und Demo-Video. Firmen tun das, kleine
 *   quelloffene Projekte nicht.
 *
 * Deshalb traegt jeder Eintrag beides: ob er quelloffen ist, und ob er
 * wirklich steuern kann. Die Oberflaeche sagt es dem Nutzer, statt ihm eine
 * Wahl vorzugaukeln, die er nicht hat.
 */
enum class Fernhilfeart(
    val paket: String,
    val anzeigename: String,
    val quelloffen: Boolean,
    /** Ob der Helfer tippen und wischen kann - nicht nur zusehen. */
    val kannSteuern: Boolean,
    val bezugsquelle: String,
    /**
     * Manche brauchen ein zweites Paket, damit der Helfer tippen kann.
     *
     * TeamViewer schiebt den Bedienhilfe-Dienst in ein eigenes Add-On statt in
     * die Hauptanwendung - erkennbar das Muster, mit dem die grossen Anbieter
     * durch Googles Pruefung kommen.
     */
    val zusatzPaket: String? = null,
    val zusatzName: String? = null,
    val zusatzQuelle: String? = null,
    /**
     * Ob der Helfer ein Passwort braucht.
     *
     * RustDesk und HopToDesk verlangen eines - und ohne ein *festes* muss bei
     * jeder Sitzung ein wechselndes vorgelesen werden, woran Hilfe am Telefon
     * regelmaessig scheitert. TeamViewer QuickSupport und AnyDesk kommen ohne
     * aus: Dort bestaetigt die Betroffene die Anfrage am Geraet.
     */
    val brauchtPasswort: Boolean = true,
) {
    /**
     * Quelloffen und selbst hostbar, aber aus dem Play Store zurueckgezogen -
     * das Projekt hat das 2024 selbst getan, weil Betrueger die App
     * missbraucht haben. Genau deshalb bleibt der Bedienhilfe-Schalter
     * gesperrt: Am 2026-10-08 nachgewiesen, auch nach einer Installation
     * ueber F-Droid.
     */
    RUSTDESK(
        paket = "com.carriez.flutter_hbb",
        anzeigename = "RustDesk",
        quelloffen = true,
        kannSteuern = false,
        bezugsquelle = "https://rustdesk.com/download",
    ),

    /**
     * Abspaltung von RustDesk, ebenfalls AGPL und selbst hostbar, aber im
     * Play Store - damit entfaellt das Sperrtheater. Der Preis steht in der
     * eigenen Dokumentation des Projekts: Die Play-Fassung kann nur zusehen,
     * weil Google die Steuerung dort nicht zulaesst.
     */
    HOPTODESK(
        paket = "com.hoptodesk.app",
        anzeigename = "HopToDesk",
        quelloffen = true,
        kannSteuern = false,
        bezugsquelle = "https://play.google.com/store/apps/details?id=com.hoptodesk.app",
    ),

    /**
     * Nicht quelloffen, aber der Weg, der wirklich steuern kann: aus dem Play
     * Store, mit Genehmigung, und mit dem Universal Add-On auch mit Tippen
     * und Wischen. Fuer private Hilfe in der Familie kostenlos.
     */
    TEAMVIEWER(
        paket = "com.teamviewer.quicksupport.market",
        anzeigename = "TeamViewer QuickSupport",
        quelloffen = false,
        kannSteuern = true,
        bezugsquelle =
        "https://play.google.com/store/apps/details?id=com.teamviewer.quicksupport.market",
        zusatzPaket = "com.teamviewer.quicksupport.addon.universal",
        zusatzName = "TeamViewer Universal Add-On",
        zusatzQuelle =
        "https://play.google.com/store/apps/details?id=com.teamviewer.quicksupport.addon.universal",
        brauchtPasswort = false,
    ),

    /** Ebenfalls nicht quelloffen, ebenfalls aus dem Play Store, Steuerung
     *  ueber ein eigenes Zusatzmodul. */
    ANYDESK(
        paket = "com.anydesk.anydeskandroid",
        anzeigename = "AnyDesk",
        quelloffen = false,
        kannSteuern = true,
        bezugsquelle =
        "https://play.google.com/store/apps/details?id=com.anydesk.anydeskandroid",
        brauchtPasswort = false,
    ),
    ;

    companion object {
        /**
         * Die Vorauswahl, wenn noch nichts installiert ist.
         *
         * TeamViewer QuickSupport, nach der Erprobung am 2026-10-08: Es ist
         * das einzige der vier, mit dem ein Helfer auf dem Geraet wirklich
         * *etwas einstellen* kann. Die beiden quelloffenen koennen nur
         * zusehen - RustDesk, weil Android den Bedienhilfe-Dienst ausserhalb
         * des Play Stores sperrt, HopToDesk, weil es ihn im Play Store
         * weglassen muss.
         *
         * Die Entscheidung gegen quelloffen faellt hier bewusst und
         * widerwillig. Wer nur zusehen und per Telefon lotsen will, ist mit
         * HopToDesk besser bedient - die Oberflaeche sagt das auch.
         */
        val VORSCHLAG = TEAMVIEWER
    }
}

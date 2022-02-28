package com.example.hikingapp

enum class TransliterationRules(val rule:String) {

    GR("η > h; ή > h; ι > i; ί > i; γκ > ng;::Any-Latin; ::nfd; ::[:nonspacing mark:] remove; ::nfc;"),
    BG("Ъ > A; ::Bulgarian-Latin/BGN; ::nfd; ::[:nonspacing mark:] remove; ::nfc;"),
    DE("::Any-Latin;\$beforeLower = [[:Mn:][:Me:]]* [:Lowercase:];\\ß > ss;"),
    EN("Any-Latin"),
    DEFAULT("Any-Latin");

}

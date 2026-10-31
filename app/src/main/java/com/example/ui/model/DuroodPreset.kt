package com.example.ui.model

data class DuroodInfo(
    val id: String,
    val title: String,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val merit: String
)

val DUROOD_PRESETS = listOf(
    DuroodInfo(
        id = "ibrahimi",
        title = "Durood-e-Ibrahimi",
        arabic = "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَعَلَىٰ آلِ مُحَمَّدٍ كَمَا صَلَّيْتَ عَلَىٰ إِبْرَاهِيمَ وَعَلَىٰ آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ، اللَّهُمَّ بَارِكْ عَلَىٰ مُحَمَّدٍ وَعَلَىٰ آلِ مُحَمَّدٍ كَمَا بَارَكْتَ عَلَىٰ إِبْرَاهِيمَ وَعَلَىٰ آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ",
        transliteration = "Allāhumma ṣalli ʿalā Muḥammadin wa-ʿalā āli Muḥammad, kamā ṣallayta ʿalā Ibrāhīma wa-ʿalā āli Ibrāhīm, innaka Ḥamīdun Majīd...",
        translation = "O Allah, bestow Your favor upon Muhammad and upon the family of Muhammad as You bestowed Your favor upon Ibrahim and upon the family of Ibrahim. You are Praiseworthy and Glorious.",
        merit = "The most esteemed and blessed Durood recited in every Salah."
    ),
    DuroodInfo(
        id = "short",
        title = "Sallallahu Alayhi Wasallam",
        arabic = "صَلَّى اللّٰهُ عَلَيْهِ وَسَلَّمَ",
        transliteration = "Ṣallallāhu ʿalayhi wa-sallam",
        translation = "May the peace and blessings of Allah be upon him.",
        merit = "Concise and profound for continuous remembrance and high counts."
    ),
    DuroodInfo(
        id = "tunajjina",
        title = "Durood-e-Tunajjina",
        arabic = "اللَّهُمَّ صَلِّ عَلَىٰ سَيِّدِنَا مُحَمَّدٍ صَلَاةً تُنَجِّينَا بِهَا مِنْ جَمِيعِ الْأَهْوَالِ وَالْآفَاتِ، وَتَقْضِي لَنَا بِهَا جَمِيعَ الْحَاجَاتِ",
        transliteration = "Allāhumma ṣalli ʿalā Sayyidinā Muḥammadin ṣalātan tunajjīnā bihā min jamīʿi al-ahwāli wal-āfāt...",
        translation = "O Allah, send blessings upon our Master Muhammad, a blessing by which You deliver us from all fears and adversities and fulfill all our needs.",
        merit = "Recited for safety, relief from difficulties, and elevation."
    ),
    DuroodInfo(
        id = "rizq",
        title = "Durood for Barakah & Mercy",
        arabic = "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ عَبْدِكَ وَرَسُولِكَ وَصَلِّ عَلَى الْمُؤْمِنِينَ وَالْمُؤْمِنَاتِ وَالْمُسْلِمِينَ وَالْمُسْلِمَاتِ",
        transliteration = "Allāhumma ṣalli ʿalā Muḥammadin ʿabdika wa-rasūlik, wa-ṣalli ʿalal-muʾminīna wal-muʾmināt...",
        translation = "O Allah, send peace upon Muhammad, Your servant and messenger, and bless all believing men and women.",
        merit = "A source of abundance, spiritual nourishment, and mercy."
    )
)

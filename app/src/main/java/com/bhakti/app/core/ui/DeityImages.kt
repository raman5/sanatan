package com.bhakti.app.core.ui

import com.bhakti.app.R
import com.bhakti.app.data.model.Deity

/**
 * Generated devotional artwork (traditional calendar-art style) for each
 * launch deity - two distinct pieces per deity so wallpaper/status grids
 * don't repeat the same image. Swap individual entries for licensed/CMS art
 * later without touching call sites - everything reads through [imageFor].
 */
fun imageFor(deity: Deity, variant: Int = 0): Int = if (variant % 2 == 0) primaryImage(deity) else secondaryImage(deity)

private fun primaryImage(deity: Deity): Int = when (deity) {
    Deity.SHIVA -> R.drawable.deity_shiva
    Deity.KRISHNA -> R.drawable.deity_krishna
    Deity.RAM -> R.drawable.deity_ram
    Deity.HANUMAN -> R.drawable.deity_hanuman
    Deity.GANESH -> R.drawable.deity_ganesh
    Deity.DURGA -> R.drawable.deity_durga
    Deity.LAKSHMI -> R.drawable.deity_lakshmi
    Deity.SARASWATI -> R.drawable.deity_saraswati
    Deity.VISHNU -> R.drawable.deity_vishnu
    Deity.RADHA -> R.drawable.deity_radha
    Deity.SAI_BABA -> R.drawable.deity_sai_baba
    Deity.JAGANNATH -> R.drawable.deity_jagannath
    Deity.SHANI_DEV -> R.drawable.deity_shani_dev
    Deity.KALI -> R.drawable.deity_kali
    Deity.BALAJI -> R.drawable.deity_balaji
}

private fun secondaryImage(deity: Deity): Int = when (deity) {
    Deity.SHIVA -> R.drawable.deity_shiva_2
    Deity.KRISHNA -> R.drawable.deity_krishna_2
    Deity.RAM -> R.drawable.deity_ram_2
    Deity.HANUMAN -> R.drawable.deity_hanuman_2
    Deity.GANESH -> R.drawable.deity_ganesh_2
    Deity.DURGA -> R.drawable.deity_durga_2
    Deity.LAKSHMI -> R.drawable.deity_lakshmi_2
    Deity.SARASWATI -> R.drawable.deity_saraswati_2
    Deity.VISHNU -> R.drawable.deity_vishnu_2
    Deity.RADHA -> R.drawable.deity_radha_2
    Deity.SAI_BABA -> R.drawable.deity_sai_baba_2
    Deity.JAGANNATH -> R.drawable.deity_jagannath_2
    Deity.SHANI_DEV -> R.drawable.deity_shani_dev_2
    Deity.KALI -> R.drawable.deity_kali_2
    Deity.BALAJI -> R.drawable.deity_balaji_2
}

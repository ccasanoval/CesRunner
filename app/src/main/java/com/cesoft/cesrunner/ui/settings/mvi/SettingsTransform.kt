package com.cesoft.cesrunner.ui.settings.mvi

import com.adidas.mvi.sideeffects.SideEffects
import com.adidas.mvi.transform.SideEffectTransform
import com.adidas.mvi.transform.ViewTransform

internal object SettingsTransform {

    data class AddSideEffect(
        val sideEffect: SettingsSideEffect
    ): SideEffectTransform<SettingsState, SettingsSideEffect>() {
        override fun mutate(sideEffects: SideEffects<SettingsSideEffect>): SideEffects<SettingsSideEffect> {
            return sideEffects.add(sideEffect)
        }
    }
}
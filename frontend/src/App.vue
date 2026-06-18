<template>
  <n-config-provider :theme="theme.dark ? darkTheme : null" :theme-overrides="themeOverrides">
    <n-message-provider>
      <router-view />
    </n-message-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import { darkTheme, type GlobalThemeOverrides } from 'naive-ui'
import { useThemeStore } from './stores/theme'

const theme = useThemeStore()

const themeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#5f7387',
    primaryColorHover: '#46586a',
    primaryColorPressed: '#334252',
    primaryColorSuppl: '#5f7387',
    borderRadius: '16px',
  },
  Button: {
    textColor: '#263445',
    textColorHover: '#101820',
    textColorPressed: '#101820',
    textColorFocus: '#263445',
    textColorDisabled: 'rgba(100, 116, 139, .58)',
    border: '1px solid rgba(38, 52, 69, .24)',
    borderHover: '1px solid rgba(38, 52, 69, .42)',
    borderPressed: '1px solid rgba(38, 52, 69, .5)',
    borderFocus: '1px solid rgba(38, 52, 69, .38)',
    borderDisabled: '1px solid rgba(100, 116, 139, .18)',
    color: 'rgba(255, 255, 255, .68)',
    colorHover: 'rgba(255, 255, 255, .88)',
    colorPressed: 'rgba(243, 246, 250, .95)',
    colorDisabled: 'rgba(100, 116, 139, .08)',
    textColorPrimary: '#ffffff',
    textColorHoverPrimary: '#ffffff',
    textColorPressedPrimary: '#ffffff',
    textColorFocusPrimary: '#ffffff',
    colorPrimary: '#5f7387',
    colorHoverPrimary: '#46586a',
    colorPressedPrimary: '#334252',
    colorFocusPrimary: '#5f7387',
    borderPrimary: '1px solid rgba(255, 255, 255, .28)',
    borderHoverPrimary: '1px solid rgba(255, 255, 255, .42)',
    borderPressedPrimary: '1px solid rgba(255, 255, 255, .32)',
    borderFocusPrimary: '1px solid rgba(255, 255, 255, .42)',
  },
}
</script>

# Accessibility checklist (Vocal)

Vocal is an AAC app. Accessibility is a core requirement, not an afterthought.

## Touch and motor

- Primary board cells: minimum **56dp** width and height
- Secondary controls: minimum **48dp**
- Adequate spacing between cells (8dp minimum in grids)
- Avoid gestures as the only way to perform essential actions

## Screen readers (TalkBack)

- Every interactive element has a meaningful `contentDescription`
- For board cells, prefer the **spoken phrase** as the description
- Announce state changes when possible (future: spoken confirmation)

## Visual

- Support system font scaling
- Sufficient color contrast for text on buttons
- Do not rely on color alone to convey meaning

## Speech output

- TTS is the default; recorded audio is optional per phrase
- Respect user speech rate preferences (DataStore)
- Fail gracefully if TTS is unavailable on device

## Cognitive load

- Simple navigation: board for users, settings for caregivers
- Consistent layout: label on button, spoken text may differ
- Avoid clutter on the main board screen

## Testing

Before merging UI changes:

1. Enable TalkBack and traverse the board
2. Test on a tablet-sized emulator (e.g. 10" API 34)
3. Verify tap targets feel comfortable at arm's length

## Future work

- High-contrast theme toggle
- Lock caregiver settings behind PIN
- Export/import boards for backup

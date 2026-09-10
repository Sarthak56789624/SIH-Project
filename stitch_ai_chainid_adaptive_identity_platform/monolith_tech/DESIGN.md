---
name: Monolith Tech
colors:
  surface: '#fdf8f8'
  surface-dim: '#ddd9d8'
  surface-bright: '#fdf8f8'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f7f3f2'
  surface-container: '#f1edec'
  surface-container-high: '#ebe7e6'
  surface-container-highest: '#e5e2e1'
  on-surface: '#1c1b1b'
  on-surface-variant: '#444748'
  inverse-surface: '#313030'
  inverse-on-surface: '#f4f0ef'
  outline: '#747878'
  outline-variant: '#c4c7c7'
  surface-tint: '#5f5e5e'
  primary: '#000000'
  on-primary: '#ffffff'
  primary-container: '#1c1b1b'
  on-primary-container: '#858383'
  inverse-primary: '#c8c6c5'
  secondary: '#585f6c'
  on-secondary: '#ffffff'
  secondary-container: '#dce2f3'
  on-secondary-container: '#5e6572'
  tertiary: '#000000'
  on-tertiary: '#ffffff'
  tertiary-container: '#1c1b1a'
  on-tertiary-container: '#868381'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#e5e2e1'
  primary-fixed-dim: '#c8c6c5'
  on-primary-fixed: '#1c1b1b'
  on-primary-fixed-variant: '#474646'
  secondary-fixed: '#dce2f3'
  secondary-fixed-dim: '#c0c7d6'
  on-secondary-fixed: '#151c27'
  on-secondary-fixed-variant: '#404754'
  tertiary-fixed: '#e6e1df'
  tertiary-fixed-dim: '#cac6c3'
  on-tertiary-fixed: '#1c1b1a'
  on-tertiary-fixed-variant: '#484645'
  background: '#fdf8f8'
  on-background: '#1c1b1b'
  surface-variant: '#e5e2e1'
typography:
  display-hero:
    fontFamily: Geist
    fontSize: 110px
    fontWeight: '700'
    lineHeight: 100px
    letterSpacing: -0.04em
  display-hero-mobile:
    fontFamily: Geist
    fontSize: 56px
    fontWeight: '700'
    lineHeight: 54px
    letterSpacing: -0.03em
  headline-lg:
    fontFamily: Geist
    fontSize: 48px
    fontWeight: '600'
    lineHeight: 56px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Geist
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: -0.01em
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
    letterSpacing: 0em
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0em
  label-sm:
    fontFamily: Geist
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.05em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  unit: 8px
  container-max: 1280px
  gutter: 24px
  margin-mobile: 20px
  margin-desktop: 64px
  section-gap: 160px
---

## Brand & Style

This design system is built on a foundation of **Technical Minimalism** and **Editorial Precision**. It targets a high-end tech audience that values security, clarity, and intelligence. The aesthetic prioritizes a "content-first" approach where typography acts as the primary visual driver, replacing traditional imagery or heavy decorative elements.

The UI should evoke a sense of calm authority. By utilizing extreme whitespace and a restrained monochromatic palette, the interface feels intentional and curated. The style draws from Swiss International Typographic Style, emphasizing cleanliness, readability, and a strict adherence to a grid, while introducing asymmetric layouts to maintain a modern, startup-centric energy.

**Key Principles:**
- **Asymmetric Balance:** Use offset grid placements to create visual interest without clutter.
- **Typographic Scale:** Leverage high contrast between massive display type and functional body text.
- **Restraint:** Every element must serve a functional purpose; if it doesn't aid the user's journey, it is removed.

## Colors

The palette is anchored by **Deep Charcoal** on a **Warm White** base, creating a high-contrast environment that is softer on the eyes than pure black-on-white. 

- **Primary (#121212):** Used for all primary text, headings, and high-impact UI elements like primary buttons.
- **Background (#F9F8F6):** A sophisticated off-white that provides a premium, "paper-like" editorial feel.
- **Secondary (#6B7280):** Reserved for meta-data, captions, and secondary icons to maintain hierarchy.
- **Accent (#2563EB):** Used sparingly for critical calls to action, active states, and data visualizations.
- **System Colors:** Muted and desaturated versions of green, amber, and red to ensure they don't disrupt the monochromatic harmony while remaining functional.

## Typography

Typography is the most critical element of this design system. We use **Geist** for headlines and labels to leverage its technical, precise geometry, and **Inter** for body text to ensure maximum readability in data-heavy contexts.

- **Display Hero:** Used for landing page headlines. It should be set with tight tracking and leading to create a "block" of text.
- **Editorial Labels:** Small, uppercase labels with increased letter spacing should be used above headlines to categorize content.
- **Hierarchy:** Maintain a clear distinction between levels. Headlines should be bold and dark (#121212), while body text can occasionally use the secondary gray (#6B7280) for lower-priority information.

## Layout & Spacing

The layout philosophy follows a **Fixed-Grid System** on desktop and a **Fluid-Grid** on mobile.

- **Grid:** Use a 12-column grid for desktop. To achieve the "editorial" look, elements should often be intentionally misaligned—for example, a headline spanning columns 1-8 while the supporting body text begins at column 5.
- **Whitespace:** Large vertical gaps (`section-gap`) are required between major content blocks to allow the eye to rest and emphasize the premium nature of the brand.
- **Breakpoints:**
  - **Mobile:** 0 - 767px (4 columns, 20px margins)
  - **Tablet:** 768px - 1023px (8 columns, 40px margins)
  - **Desktop:** 1024px+ (12 columns, 64px margins)

## Elevation & Depth

This design system avoids traditional shadows to maintain a flat, modernist aesthetic. Depth is communicated through **Tonal Layering** and **Line Work**.

- **Surface Layers:** The background is `#F9F8F6`. Elevated elements (like cards or modals) should use the same color or a pure white (#FFFFFF) but be defined by a thin, 1px border in `#121212` (at 10% opacity) rather than a shadow.
- **Interactions:** When an element is hovered, the border opacity can increase or the background can shift slightly to a light gray (#F2F1EF). 
- **Zero-Shadow Policy:** No drop shadows or inner shadows are permitted. Visual hierarchy must be achieved through size, color contrast, and spacing.

## Shapes

The shape language is **Sharp and Disciplined**. We use a very low radius to avoid the "bubbliness" of consumer apps, maintaining a professional, technical appearance.

- **Small Components:** Inputs and buttons use a 4px (0.25rem) radius.
- **Large Components:** Cards and containers use an 8px (0.5rem) radius.
- **Icons:** Use stroke-based icons with a 1.5pt or 2pt weight to match the precision of the Geist typeface. Corners in iconography should also be slightly rounded to match the UI components.

## Components

### Buttons
- **Primary:** Solid `#121212` background with `#F9F8F6` text. Sharp corners (4px). No gradient.
- **Secondary:** Ghost style. Transparent background with a 1px border of `#121212`.
- **Accent:** Solid `#2563EB` only for primary conversion actions.

### Input Fields
- Underline style or fully enclosed with a 1px light border. Focus state is indicated by a weight change in the border or a transition to the primary Deep Charcoal color. Label sits above in `label-sm` style.

### Cards
- No shadows. Use a 1px border (#121212 at 10% opacity). Cards should have generous internal padding (32px or 40px) to maintain the airy editorial feel.

### Status Indicators (Chips)
- Small, rectangular chips with `label-sm` typography. Use background colors at 10% opacity of the system colors (Green, Amber, Red) with high-contrast text for accessibility.

### Motion
- Transitions should be fast but smooth (200-300ms). Use "Ease-Out" for entering elements. Prefer subtle opacity fades and slight Y-axis translations (4px) over dramatic animations.
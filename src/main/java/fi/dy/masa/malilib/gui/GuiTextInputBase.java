package fi.dy.masa.malilib.gui;

import javax.annotation.Nullable;
import org.lwjgl.input.Keyboard;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.StringUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;

public abstract class GuiTextInputBase extends GuiDialogBase
{
    protected final GuiTextField textField;
    protected final String originalText;

    public GuiTextInputBase(int maxTextLength, String titleKey, String defaultText, @Nullable GuiScreen parent)
    {
        this.setParent(parent);
        this.title = StringUtils.translate(titleKey);
        this.useTitleHierarchy = false;
        this.originalText = defaultText;

        this.setWidthAndHeight(260, 100);
        this.centerOnScreen();

        int width = Math.min(maxTextLength * 10, 240);
        this.textField = new GuiTextFieldGeneric(this.dialogLeft + 12, this.dialogTop + 40, width, 20, this.textRenderer);
        this.textField.setMaxStringLength(maxTextLength);
        this.textField.setFocused(true);
        this.textField.setText(this.originalText);
        this.textField.setCursorPositionEnd();
        this.zLevel = 1f;
    }

    @Override
    public void initGui()
    {
        int x = this.dialogLeft + 10;
        int y = this.dialogTop + 70;

        x += this.createButton(x, y, ButtonType.OK) + 2;
        x += this.createButton(x, y, ButtonType.RESET) + 2;
        x += this.createButton(x, y, ButtonType.CANCEL) + 2;

        Keyboard.enableRepeatEvents(true);
    }

    protected int createButton(int x, int y, ButtonType type)
    {
        ButtonGeneric button = new ButtonGeneric(x, y, -1, 20, type.getDisplayName());
        button.setWidth(Math.max(40, button.getWidth()));
        return this.addButton(button, this.createActionListener(type)).getWidth();
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return this.getParent() != null && this.getParent().doesGuiPauseGame();
    }

    @Override
    public void drawContents(int mouseX, int mouseY, float partialTicks)
    {
        if (this.getParent() != null)
        {
            this.getParent().drawScreen(mouseX, mouseY, partialTicks);
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, this.zLevel);

        RenderUtils.drawOutlinedBox(this.dialogLeft, this.dialogTop, this.dialogWidth, this.dialogHeight, 0xE0000000, COLOR_HORIZONTAL_BAR);

        // Draw the title
        this.drawStringWithShadow(this.getTitle(), this.dialogLeft + 10, this.dialogTop + 4, COLOR_WHITE);

        //super.drawScreen(mouseX, mouseY, partialTicks);
        this.textField.drawTextBox();

        this.drawButtons(mouseX, mouseY, partialTicks);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean onKeyTyped(char typedChar, int keyCode)
    {
        if (keyCode == Keyboard.KEY_RETURN)
        {
            // Only close the GUI if the value was successfully applied
            if (this.applyValue(this.textField.getText()))
            {
                GuiBase.openGui(this.getParent());
            }

            return true;
        }
        else if (keyCode == Keyboard.KEY_ESCAPE)
        {
            GuiBase.openGui(this.getParent());
            return true;
        }

        if (this.textField.isFocused())
        {
            return this.textField.textboxKeyTyped(typedChar, keyCode);
        }

        return super.onKeyTyped(typedChar, keyCode);
    }

    @Override
    public boolean onMouseClicked(int mouseX, int mouseY, int button)
    {
        if (this.textField.mouseClicked(mouseX, mouseY, button))
        {
            return true;
        }

        return super.onMouseClicked(mouseX, mouseY, button);
    }

    protected ButtonListener createActionListener(ButtonType type)
    {
        return new ButtonListener(type, this);
    }

    protected abstract boolean applyValue(String string);

    protected static class ButtonListener implements IButtonActionListener
    {
        private final GuiTextInputBase gui;
        private final ButtonType type;

        public ButtonListener(ButtonType type, GuiTextInputBase gui)
        {
            this.type = type;
            this.gui = gui;
        }

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton)
        {
            if (this.type == ButtonType.OK)
            {
                // Only close the GUI if the value was successfully applied
                if (this.gui.applyValue(this.gui.textField.getText()))
                {
                    GuiBase.openGui(this.gui.getParent());
                }
            }
            else if (this.type == ButtonType.CANCEL)
            {
                GuiBase.openGui(this.gui.getParent());
            }
            else if (this.type == ButtonType.RESET)
            {
                this.gui.textField.setText(this.gui.originalText);
                this.gui.textField.setCursorPosition(0);
                this.gui.textField.setFocused(true);
            }
        }
    }

    protected enum ButtonType
    {
        OK      ("malilib.gui.button.ok"),
        CANCEL  ("malilib.gui.button.cancel"),
        RESET   ("malilib.gui.button.reset");

        private final String labelKey;

        private ButtonType(String labelKey)
        {
            this.labelKey = labelKey;
        }

        public String getDisplayName()
        {
            return StringUtils.translate(this.labelKey);
        }
    }
}

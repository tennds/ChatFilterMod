package chatfilter.mixin;

import chatfilter.ChatFilterStore;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts every message added to the chat and cancels it
 * if its text contains an ignored nickname.
 */
@Mixin(ChatComponent.class)
public class ChatComponentMixin {

    @Inject(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void chatfilter$hideIgnored(Component message, MessageSignature signature,
                                        GuiMessageSource source, GuiMessageTag tag,
                                        CallbackInfo ci) {
        if (message != null && ChatFilterStore.shouldHide(message.getString())) {
            ci.cancel();
        }
    }
}
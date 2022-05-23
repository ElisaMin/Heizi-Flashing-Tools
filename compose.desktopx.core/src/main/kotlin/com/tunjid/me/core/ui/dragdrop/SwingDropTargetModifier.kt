/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tunjid.me.core.ui.dragdrop

import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.OnGloballyPositionedModifier
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.modifier.*
import androidx.compose.ui.platform.InspectorValueInfo
import androidx.compose.ui.platform.debugInspectorInfo
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.*
import java.io.File
import java.awt.dnd.DropTarget as AwtDropTarget

//class PlatformDropTargetModifier(
//    density: Float,
//    window: ComposeWindow,
//) : DropTargetModifier by dropTargetModifier() {
//    init {
//        val awtDropTarget = AwtDropTarget()
//        awtDropTarget.addDropTargetListener(
//            dropTargetListener(
//                dropTargetModifier = this,
//                density = density
//            )
//        )
//        window.contentPane.dropTarget = awtDropTarget
//    }
//}

private fun dropTargetListener(
    dropTargetModifier: DropTargetModifier,
    density: Float
) = object : DropTargetListener {
    override fun dragEnter(dtde: DropTargetDragEvent?) {
        if (dtde == null) return
        dropTargetModifier.onDragStarted(
            listOf(),
            Offset(
                dtde.location.x * density,
                dtde.location.y * density
            )
        )
        dropTargetModifier.onDragEntered()
    }

    override fun dragOver(dtde: DropTargetDragEvent?) {
        if (dtde == null) return
        dropTargetModifier.onDragMoved(
            Offset(
                dtde.location.x * density,
                dtde.location.y * density
            )
        )
    }

    override fun dropActionChanged(dtde: DropTargetDragEvent?) = Unit

    override fun dragExit(dte: DropTargetEvent?) {
        dropTargetModifier.onDragExited()
        dropTargetModifier.onDragEnded()
    }

    override fun drop(dtde: DropTargetDropEvent?) {
        if (dtde == null) return dropTargetModifier.onDragEnded()

        dtde.acceptDrop(DnDConstants.ACTION_REFERENCE)
        dtde.dropComplete(
            dropTargetModifier.onDropped(
                dtde.fileUris(),
                Offset(
                    dtde.location.x * density,
                    dtde.location.y * density
                )
            )
        )
        dropTargetModifier.onDragEnded()
    }
}

private fun DropTargetDropEvent.fileUris(): List<Uri> = transferable
    .getTransferData(DataFlavor.javaFileListFlavor)
    .let { it as? List<*> ?: listOf<File>() }
    .filterIsInstance<File>()
    .map(::FileUri)



interface DropTarget {
    fun onDragStarted(uris: List<Uri>, position: Offset): Boolean
    fun onDragEntered()
    fun onDragMoved(position: Offset) {}
    fun onDragExited()
    fun onDropped(uris: List<Uri>, position: Offset): Boolean
    fun onDragEnded()
}

interface DropTargetModifier : DropTarget, Modifier.Element

class PlatformDropTargetModifier(
    density: Float,
    window: ComposeWindow,
) : DropTargetModifier by dropTargetModifier() {
    init {
        val awtDropTarget = AwtDropTarget()
        awtDropTarget.addDropTargetListener(
            dropTargetListener(
                dropTargetModifier = this,
                density = density
            )
        )
        window.contentPane.dropTarget = awtDropTarget
    }
}

internal fun dropTargetModifier(): DropTargetModifier = DropTargetContainer(
    onDragStarted = { _, _ -> DragAction.Reject }
)


fun Modifier.dropTarget(
    onDragStarted: (uris: List<Uri>, Offset) -> Boolean,
    onDragEntered: () -> Unit = { },
    onDragMoved: (position: Offset) -> Unit = {},
    onDragExited: () -> Unit = { },
    onDropped: (uris: List<Uri>, position: Offset) -> Boolean,
    onDragEnded: () -> Unit = {},
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "dropTarget"
        properties["onDragStarted"] = onDragStarted
    },
    factory = {
        val node = remember {
            DropTargetContainer { uris, offset ->
                when (onDragStarted(uris, offset)) {
                    false -> DragAction.Reject
                    true -> DragAction.Accept(
                        object : DropTarget {
                            override fun onDragStarted(uris: List<Uri>, position: Offset): Boolean = onDragStarted(
                                uris,
                                position
                            )

                            override fun onDragEntered() = onDragEntered()

                            override fun onDragMoved(position: Offset) = onDragMoved(position)

                            override fun onDragExited() = onDragExited()

                            override fun onDropped(uris: List<Uri>, position: Offset): Boolean = onDropped(
                                uris,
                                position
                            )

                            override fun onDragEnded() = onDragEnded()
                        }
                    )
                }
            }
        }
        this.then(node)
    })

internal val ModifierLocalDropTargetParent = modifierLocalOf<DropTargetParent?> { null }

internal interface DropTargetParent {
    fun registerChild(child: DropTargetChild)
    fun unregisterChild(child: DropTargetChild)
}

internal interface DropTargetChild : DropTarget {
    fun contains(position: Offset): Boolean
}

internal sealed class DragAction {
    object Reject : DragAction()

    data class Accept(val dropTarget: DropTarget) : DragAction()

    internal val target: DropTarget?
        get() = when (this) {
            Reject -> null
            is Accept -> dropTarget
        }
}

internal class DropTargetContainer(
    private val onDragStarted: (uris: List<Uri>, Offset) -> DragAction
) : Modifier.Element,
    ModifierLocalConsumer,
    ModifierLocalProvider<DropTargetParent?>,
    OnGloballyPositionedModifier,
    RememberObserver,
    DropTargetParent,
    DropTargetChild,
    DropTargetModifier,
    InspectorValueInfo(debugInspectorInfo {
        name = "dropTarget"
        properties["onDragStarted"] = onDragStarted
    }) {

    private var parent: DropTargetParent? = null
        set(value) {
            if (value != field) {
                field?.unregisterChild(this)
                field = value
                field?.registerChild(this)
            }
        }
    private val children = mutableListOf<DropTargetChild>()
    private var coordinates: LayoutCoordinates? = null
    private var activeChild: DropTargetChild? = null
    private var currentTarget: DropTarget? = null

    // start ModifierLocalProvider
    override val key: ProvidableModifierLocal<DropTargetParent?>
        get() = ModifierLocalDropTargetParent

    override val value: DropTargetContainer
        get() = this
    // end ModifierLocalProvider

    // start ModifierLocalConsumer
    override fun onModifierLocalsUpdated(scope: ModifierLocalReadScope) {
        parent = with(scope) { key.current }
    }
    // end ModifierLocalConsumer

    // start RememberObserver
    override fun onRemembered() {}

    override fun onForgotten() {
        parent = null
        currentTarget = null
    }

    override fun onAbandoned() {}
    // end RememberObserver

    // start DropTargetParent
    override fun registerChild(child: DropTargetChild) {
        children += child
        // I can't ToODO if a drag is in progress, check if we need to send events
    }

    override fun unregisterChild(child: DropTargetChild) {
        children -= child
    }
    // end DropTargetParent

    // start DropTargetNode
    override fun contains(position: Offset): Boolean {
        val currentCoordinates = coordinates ?: return false
        if (!currentCoordinates.isAttached) return false

        val (width, height) = currentCoordinates.size
        val (x1, y1) = currentCoordinates.positionInRoot()
        val x2 = x1 + width
        val y2 = y1 + height

        return position.x in x1..x2 && position.y in y1..y2
    }

    override fun onDragStarted(uris: List<Uri>, position: Offset): Boolean {
        coordinates ?: return false

        check(currentTarget == null)
        currentTarget = onDragStarted.invoke(uris, position).target

        var handledByChild = false

        children.forEach { child ->
            handledByChild = handledByChild or child.onDragStarted(
                uris = uris,
                position = position
            )
        }
        return handledByChild || currentTarget != null
    }
    // end DropTargetNode

    // start OnGloballyPositionedModifier
    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        this.coordinates = coordinates
    }
    // end OnGloballyPositionedModifier

    // start DropTarget
    override fun onDragEntered() {
        currentTarget?.onDragEntered()
    }

    override fun onDragMoved(position: Offset) {
        coordinates ?: return
        val currentActiveChild: DropTargetChild? = activeChild

        val newChild: DropTargetChild? = when (currentActiveChild != null && currentActiveChild.contains(position)) {
            // Moved within child.
            true -> currentActiveChild
            // Position is now outside active child, maybe it entered a different one.
            false -> children.firstOrNull { it.contains(position) }
        }
        when {
            // Left us and went to a child.
            newChild != null && currentActiveChild == null -> {
                currentTarget?.onDragExited()
                newChild.dispatchEntered(position)
            }
            // Left the child and returned to us.
            newChild == null && currentActiveChild != null -> {
                currentActiveChild.onDragExited()
                currentTarget?.dispatchEntered(position)
            }
            // Left one child and entered another.
            newChild != currentActiveChild -> {
                currentActiveChild?.onDragExited()
                newChild?.dispatchEntered(position)
            }
            // Stayed in the same child.
            newChild != null -> newChild.onDragMoved(position)
            // Stayed in us.
            else -> currentTarget?.onDragMoved(position)
        }

        this.activeChild = newChild
    }

    override fun onDragExited() {
//        activeChild?.onDragExited()
//        activeChild = null
        currentTarget?.onDragExited()
    }

    override fun onDropped(uris: List<Uri>, position: Offset): Boolean =
        when (val currentActiveChild = activeChild) {
            null -> currentTarget?.onDropped(
                uris = uris,
                position = position
            ) ?: false
            else -> currentActiveChild.onDropped(
                uris = uris,
                position = position
            )
        }

    override fun onDragEnded() {
        children.forEach {
            it.onDragEnded()
        }
        currentTarget?.onDragEnded()
        currentTarget = null
    }
    // end DropTarget

    private fun DropTarget.dispatchEntered(position: Offset) {
        onDragEntered()
        onDragMoved(position)
    }
}
interface Uri {
val path: String
}
//
//expect class UriConverter() {
//    fun toInput(uri: Uri): Input
//    suspend fun name(uri: Uri): String
//}
data class FileUri(
    val file: File
) : Uri {
    override val path: String
        get() = file.path
//    override val mimeType: String?
//        get() = Files.probeContentType(file.toPath())
}
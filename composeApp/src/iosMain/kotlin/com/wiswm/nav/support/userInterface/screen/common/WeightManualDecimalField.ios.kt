package com.wiswm.nav.support.userInterface.screen.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIBarButtonItem
import platform.UIKit.UIBarButtonSystemItem
import platform.UIKit.UIButton
import platform.UIKit.UIColor
import platform.UIKit.UIControl
import platform.UIKit.UIControlEventEditingChanged
import platform.UIKit.UIControlEventTouchUpInside
import platform.UIKit.UIFont
import platform.UIKit.UILabel
import platform.UIKit.UITextBorderStyle
import platform.UIKit.UITextField
import platform.UIKit.UITextFieldViewMode
import platform.UIKit.UIToolbar
import platform.UIKit.UIView
import platform.UIKit.UIWindow
import platform.UIKit.UIApplication
import platform.darwin.NSObject

private fun dismissIosKeyboard(textField: UITextField) {
    textField.resignFirstResponder()
}

private class WeightManualOnChangeHolder {
    var onChange: (String) -> Unit = {}
}

private class WeightManualNativeRefs {
    var actions: WeightManualTextFieldActions? = null
}

@OptIn(ExperimentalForeignApi::class)
private class WeightManualTextFieldActions(
    private val holder: WeightManualOnChangeHolder,
) : NSObject() {
    lateinit var textField: UITextField

    @ObjCAction
    fun editingChanged(sender: UIControl) {
        val raw = textField.text ?: ""
        val filtered = filterManualWeightDecimal(raw) ?: return
        if (textField.text != filtered) {
            textField.text = filtered
        }
        holder.onChange(filtered)
    }

    @ObjCAction
    fun doneTapped(sender: UIControl) {
        dismissIosKeyboard(textField)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun unitTrailingAccessoryView(units: String): UIView {
    val paddingEnd = 12.0
    val containerH = 44.0
    val label = UILabel()
    label.text = units
    label.textColor = UIColor.blackColor
    label.font = UIFont.systemFontOfSize(17.0)
    label.sizeToFit()
    val labelW = label.frame.useContents { size.width }
    val labelH = label.frame.useContents { size.height }
    val containerW = labelW + paddingEnd
    val container = UIView(frame = CGRectMake(0.0, 0.0, containerW, containerH))
    label.setFrame(CGRectMake(0.0, (containerH - labelH) / 2.0, labelW, labelH))
    container.addSubview(label)
    return container
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun WeightManualDecimalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    units: String,
    modifier: Modifier,
) {
    val holder = remember { WeightManualOnChangeHolder() }
    val nativeRefs = remember { WeightManualNativeRefs() }
    holder.onChange = onValueChange

    UIKitView(
        factory = {
            val actions = WeightManualTextFieldActions(holder)
            nativeRefs.actions = actions
            val textField = UITextField(frame = CGRectMake(0.0, 0.0, 0.0, 44.0))
            actions.textField = textField

            textField.keyboardType = 8L
            textField.borderStyle = UITextBorderStyle.UITextBorderStyleNone
            textField.backgroundColor = UIColor.whiteColor
            textField.layer.cornerRadius = 4.0
            textField.layer.borderWidth = 1.0
            textField.layer.borderColor = UIColor.lightGrayColor.CGColor
            textField.textColor = UIColor.blackColor
            textField.tintColor = UIColor.blueColor
            textField.text = value
            textField.clearButtonMode = UITextFieldViewMode.UITextFieldViewModeNever

            val contentPaddingH = 12.0
            val leftPad = UIView(frame = CGRectMake(0.0, 0.0, contentPaddingH, 44.0))
            textField.leftView = leftPad
            textField.leftViewMode = UITextFieldViewMode.UITextFieldViewModeAlways
            textField.rightView = unitTrailingAccessoryView(units)
            textField.rightViewMode = UITextFieldViewMode.UITextFieldViewModeAlways

            textField.addTarget(
                target = actions,
                action = NSSelectorFromString("editingChanged:"),
                forControlEvents = UIControlEventEditingChanged
            )

            val toolbar = UIToolbar()
            toolbar.barTintColor = UIColor.whiteColor
            toolbar.tintColor = UIColor.blueColor
            toolbar.sizeToFit()

            val flex = UIBarButtonItem(
                barButtonSystemItem = UIBarButtonSystemItem.UIBarButtonSystemItemFlexibleSpace,
                target = null,
                action = null
            )

            val doneButton = UIButton.buttonWithType(1L)
            doneButton.setTitle("Done", forState = 0uL)
            doneButton.setTitleColor(UIColor.blueColor, forState = 0uL)
            doneButton.sizeToFit()
            doneButton.addTarget(
                target = actions,
                action = NSSelectorFromString("doneTapped:"),
                forControlEvents = UIControlEventTouchUpInside
            )
            val done = UIBarButtonItem(customView = doneButton)

            toolbar.setItems(listOf(flex, done), animated = false)
            textField.inputAccessoryView = toolbar
            textField
        },
        modifier = modifier
            .background(Color.White)
            .defaultMinSize(minWidth = 0.dp, minHeight = 56.dp),
        update = { textField ->
            if (textField.text != value) {
                textField.text = value
            }
            textField.rightView = unitTrailingAccessoryView(units)
            textField.layer.borderColor = UIColor.lightGrayColor.CGColor
            textField.layer.borderWidth = 1.0
        },
        onRelease = {},
        properties = UIKitInteropProperties(
            isInteractive = true,
            isNativeAccessibilityEnabled = true,
        ),
    )
}

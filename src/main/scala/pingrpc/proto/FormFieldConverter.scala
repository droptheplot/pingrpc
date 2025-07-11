package pingrpc.proto

import javafx.util.StringConverter
import pingrpc.form.FormField

class FormFieldConverter extends StringConverter[FormField] {
  override def toString(t: FormField): String = Option(t).map(_.fieldDescriptor.getName).getOrElse("")

  override def fromString(s: String): FormField = ???
}

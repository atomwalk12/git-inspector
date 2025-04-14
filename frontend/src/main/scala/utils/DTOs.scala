package utils

sealed trait IndexEvent
case object RefreshIndicesRequested extends IndexEvent

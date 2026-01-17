package errors

import (
	err "errors"
)

var ErrPatientNotFound = err.New("patient couldn't be found in the database")

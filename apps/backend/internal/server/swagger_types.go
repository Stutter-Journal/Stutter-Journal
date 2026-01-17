package server

// Swagger-visible types are exported aliases to the internal request/response DTOs.
type DoctorRegisterRequest = doctorRegisterRequest

type DoctorLoginRequest = doctorLoginRequest

type DoctorResponse = doctorResponse

type PatientRegisterRequest = patientRegisterRequest

type PatientLoginRequest = patientLoginRequest

type PatientResponse = patientResponse

type PracticeCreateRequest = practiceCreateRequest

type PracticeResponse = practiceResponse

type PracticeCreateResponse struct {
	Practice PracticeResponse `json:"practice"`
	Doctor   DoctorResponse   `json:"doctor"`
}

type LinkInviteRequest = linkInviteRequest

type LinkResponse struct {
	Link    linkDTO    `json:"link"`
	Patient patientDTO `json:"patient"`
}

type LinkApproveResponse = linkApproveResponse

type PatientsResponse = patientsListResponse

type EntriesResponse = entriesResponse

type AnalyticsResponse = analyticsResponse

type TrendPoint = trendPoint

type ErrorResponse struct {
	Error string `json:"error"`
}

type StatusResponse struct {
	Status string `json:"status"`
}

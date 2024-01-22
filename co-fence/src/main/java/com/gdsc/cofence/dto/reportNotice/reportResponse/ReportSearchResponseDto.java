package com.gdsc.cofence.dto.reportNotice.reportResponse;

import com.gdsc.cofence.entity.report.ReportManagement;
import com.gdsc.cofence.entity.workplace.WorkPlace;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Optional;

@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ReportSearchResponseDto {

    private Long reportId;
    private String reportStatus;
    private String reportSubject;
    private LocalDateTime createdAt;
    private Long reportedWorkplaceId;

    public ReportSearchResponseDto(ReportManagement reportManagement) {
        this.reportId = reportManagement.getReportId();
        this.reportSubject = reportManagement.getReportSubject();
        this.createdAt = reportManagement.getCreatedAt();
        this.reportStatus = reportManagement.getReportStatus().getDisplayName();
        if (reportManagement.getReportedWorkplace() != null) {
            this.reportedWorkplaceId = reportManagement.getReportedWorkplace().getWorkplaceId();
        } else {
            this.reportedWorkplaceId = null;
        }
    }
}

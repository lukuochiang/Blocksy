package com.blocksy.server.modules.report.service;

import com.blocksy.server.modules.report.dto.AdminReportHandleRequest;
import com.blocksy.server.modules.report.dto.AdminReportResponse;
import com.blocksy.server.modules.report.dto.ReportCreateRequest;
import com.blocksy.server.modules.report.dto.ReportResponse;

import java.util.List;

public interface ReportService {
    ReportResponse create(Long reporterUserId, ReportCreateRequest request);

    List<AdminReportResponse> listForAdmin(String processStatus);

    AdminReportResponse handleForAdmin(Long reportId, Long handlerUserId, AdminReportHandleRequest request);
}

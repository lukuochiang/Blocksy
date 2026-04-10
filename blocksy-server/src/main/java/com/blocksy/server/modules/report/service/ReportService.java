package com.blocksy.server.modules.report.service;

import com.blocksy.server.modules.report.dto.AdminReportHandleRequest;
import com.blocksy.server.modules.report.dto.AdminReportHandleLogResponse;
import com.blocksy.server.modules.report.dto.AdminReportResponse;
import com.blocksy.server.modules.report.dto.AdminReportBatchHandleRequest;
import com.blocksy.server.modules.report.dto.AdminReportBatchHandleResponse;
import com.blocksy.server.modules.report.dto.AdminReportBatchRetryRequest;
import com.blocksy.server.modules.report.dto.ReportCreateRequest;
import com.blocksy.server.modules.report.dto.ReportResponse;

import java.util.List;

public interface ReportService {
    ReportResponse create(Long reporterUserId, ReportCreateRequest request);

    List<AdminReportResponse> listForAdmin(String processStatus);

    AdminReportResponse handleForAdmin(Long reportId, Long handlerUserId, AdminReportHandleRequest request);

    AdminReportBatchHandleResponse batchHandleForAdmin(Long handlerUserId, AdminReportBatchHandleRequest request);

    AdminReportBatchHandleResponse batchRetryForAdmin(Long handlerUserId, AdminReportBatchRetryRequest request);

    String exportBatchHandleResultCsv(AdminReportBatchHandleResponse response);

    List<AdminReportHandleLogResponse> listHandleLogsForAdmin(Long reportId);
}

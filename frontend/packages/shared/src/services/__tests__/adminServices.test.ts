import { describe, it, expect, beforeEach, vi } from 'vitest';

const httpMock = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn(),
  delete: vi.fn(),
  put: vi.fn(),
  patch: vi.fn(),
}));

vi.mock('../../utils/http', () => ({
  http: httpMock,
}));

import { rateLimitService } from '../rateLimit';
import { reportService, type HandleReportPayload } from '../report';

describe('管理端服务封装', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('rateLimitService.getRules 应调用正确接口并返回数据', async () => {
    const rules = {
      enabled: true,
      userWhitelist: [1],
      ipWhitelist: ['127.0.0.1'],
      ipBlacklist: [],
    };

    httpMock.get.mockResolvedValueOnce({ data: rules });

    const result = await rateLimitService.getRules();

    expect(httpMock.get).toHaveBeenCalledWith('/api/admin/rate-limit/rules');
    expect(result).toEqual(rules);
  });

  it('reportService.handleReport 应将处理参数拼接到查询字符串', async () => {
    const payload: HandleReportPayload = {
      approved: true,
      handleResult: '通过',
    };

    httpMock.post.mockResolvedValueOnce({ data: null });

    await reportService.handleReport(99, payload);

    expect(httpMock.post).toHaveBeenCalledWith(
      '/api/reports/99/handle',
      null,
      {
        params: {
          approved: payload.approved,
          handleResult: payload.handleResult,
        },
      }
    );
  });
});

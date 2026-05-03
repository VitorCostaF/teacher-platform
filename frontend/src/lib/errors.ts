export const HTTP_ERROR_MESSAGES: Record<number, string> = {
  400: 'Dados inválidos. Verifique os campos e tente novamente.',
  401: 'Sessão expirada. Faça login novamente.',
  403: 'Você não tem permissão para esta ação.',
  404: 'Recurso não encontrado.',
  409: 'Conflito: este registro já existe.',
  410: 'Este link não está mais disponível.',
  422: 'Dados inválidos no servidor.',
  429: 'Muitas tentativas. Aguarde um momento.',
  500: 'Erro interno do servidor. Tente novamente.',
  502: 'Serviço temporariamente indisponível.',
  503: 'Servidor em manutenção. Tente em alguns minutos.',
}

export const TIMEOUT_MESSAGE =
  'Não foi possível conectar. Verifique sua internet e tente novamente.'

export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
    public fieldErrors?: Record<string, string>,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

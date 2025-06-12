package Classes;

import org.json.JSONObject;

public class Operacao {
    private String operacao;
    private String mensagem;
    private String status;
    private JSONObject dados;

    public Operacao(String operacao, String mensagem, String status) {
        this.operacao = operacao;
        this.mensagem = mensagem;
        this.status = status;
    }

    public Operacao(JSONObject json) {
        this.operacao = json.optString("operacao");
        this.mensagem = json.optString("mensagem");
        this.status = json.optString("status");
        this.dados = json.optJSONObject("dados");
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("operacao", operacao);
        if (mensagem != null) json.put("mensagem", mensagem);
        if (status != null) json.put("status", status);
        if (dados != null) json.put("dados", dados);
        return json;
    }

    // getters e setters
    public String getOperacao() { return operacao; }
    public void setOperacao(String operacao) { this.operacao = operacao; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public JSONObject getDados() { return dados; }
    public void setDados(JSONObject dados) { this.dados = dados; }
}

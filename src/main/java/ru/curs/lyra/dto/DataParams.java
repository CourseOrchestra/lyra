package ru.curs.lyra.dto;

public final class DataParams {
    private FormInstantiationParams formInstantiationParams;
    private DataRetrievalParams dataRetrievalParams;

    public FormInstantiationParams getFormInstantiationParams() {
        return formInstantiationParams;
    }

    public void setFormInstantiationParams(FormInstantiationParams formInstantiationParams) {
        this.formInstantiationParams = formInstantiationParams;
    }

    public DataRetrievalParams getDataRetrievalParams() {
        return dataRetrievalParams;
    }

    public void setDataRetrievalParams(DataRetrievalParams dataRetrievalParams) {
        this.dataRetrievalParams = dataRetrievalParams;
    }
}

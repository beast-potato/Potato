package com.beastpotato.potato.compiler.generators;

import com.beastpotato.potato.api.Body;
import com.beastpotato.potato.api.Constants;
import com.beastpotato.potato.api.Endpoint;
import com.beastpotato.potato.api.HeaderParam;
import com.beastpotato.potato.api.UrlParam;
import com.beastpotato.potato.api.UrlPathParam;
import com.beastpotato.potato.compiler.models.ModelFieldDef;
import com.beastpotato.potato.compiler.models.RequestModel;
import com.beastpotato.potato.compiler.plugin.ProcessorLogger;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Created by Oleksiy on 2/6/2016.
 */
public class RequestModelGenerator extends BaseGenerator<RequestModel> {
    private Types typeUtils;
    private Elements elementUtils;

    private TypeElement classElement;
    private List<VariableElement> urlPathParams, urlParams, headerParams;
    private VariableElement body;

    public RequestModelGenerator(TypeElement classElement, Types typeUtils, Elements elementUtils, ProcessorLogger logger) {
        super(logger);
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
        this.classElement = classElement;
        urlPathParams = new ArrayList<>();
        urlParams = new ArrayList<>();
        headerParams = new ArrayList<>();
    }


    private void addUrlPathParam(VariableElement variableElement) {
        urlPathParams.add(variableElement);
    }

    private void addUrlParam(VariableElement variableElement) {
        urlParams.add(variableElement);
    }

    private void addHeaderParam(VariableElement variableElement) {
        headerParams.add(variableElement);
    }

    private void setBody(VariableElement variableElement) {
        body = variableElement;
    }

    private <A extends Annotation> boolean isVariableElementAnnotated(Element element, Class<A> annotation) throws InitializationException {
        if (element.getKind() != ElementKind.FIELD && element.getAnnotation(annotation) != null) {
            throw new InitializationException(String.format("Only fields can be annotated with @%1s", annotation.getSimpleName()));
        } else
            return element.getKind() == ElementKind.FIELD && element.getAnnotation(annotation) != null;
    }

    @Override
    public void initialize() throws InitializationException {
        log(Diagnostic.Kind.NOTE, "initializing...");
        for (Element element : classElement.getEnclosedElements()) {
            log(Diagnostic.Kind.NOTE, String.format("Initializing %1s", element.getSimpleName()));
            if (isVariableElementAnnotated(element, UrlPathParam.class)) {
                addUrlPathParam((VariableElement) element);
            } else if (isVariableElementAnnotated(element, UrlParam.class)) {
                addUrlParam((VariableElement) element);
            } else if (isVariableElementAnnotated(element, HeaderParam.class)) {
                addHeaderParam((VariableElement) element);
            } else if (isVariableElementAnnotated(element, Body.class)) {
                setBody((VariableElement) element);
            }
        }
        log(Diagnostic.Kind.NOTE, "initialization done.");
    }

    @Override
    public RequestModel generate() throws GenerationException {
        log(Diagnostic.Kind.NOTE, "generating model...");
        try {
            RequestModel requestModel = new RequestModel(classElement);
            log(Diagnostic.Kind.NOTE, String.format("Generating classElement"));
            parseTypeElementInto(classElement, requestModel);
            log(Diagnostic.Kind.NOTE, String.format("Generating urlPathParams"));
            parseUrlPathParams(urlPathParams, requestModel);
            log(Diagnostic.Kind.NOTE, String.format("Generating urlParams"));
            parseUrlParams(urlParams, requestModel);
            log(Diagnostic.Kind.NOTE, String.format("Generating headerParams"));
            parseHeaderParams(headerParams, requestModel);
            log(Diagnostic.Kind.NOTE, String.format("Generating body"));
            parseBody(body, requestModel);
            log(Diagnostic.Kind.NOTE, "model generated...");
            return requestModel;
        } catch (Exception e) {
            e.printStackTrace();
            throw new GenerationException(String.format("Generation failed due to %1s", e.getMessage()));
        }
    }

    private void parseTypeElementInto(TypeElement typeElement, RequestModel requestModel) {
        Endpoint annotation = typeElement.getAnnotation(Endpoint.class);
        String relativeUrl = annotation.relativeUrl();
        Constants.Http method = annotation.httpMethod();
        String exampleJson = annotation.jsonExample();
        requestModel.setMethod(method);
        requestModel.setRelativeUrl(relativeUrl);
        requestModel.setExampleJson(exampleJson);
    }

    private void parseUrlPathParams(List<VariableElement> urlPathParams, RequestModel requestModel) {
        for (VariableElement varElement : urlPathParams) {
            UrlPathParam annotation = varElement.getAnnotation(UrlPathParam.class);
            String pathParamKey = annotation.value();
            requestModel.addField(ModelFieldDef.FieldType.UrlPathParam, pathParamKey, varElement.getSimpleName().toString(), varElement.asType());
        }
    }

    private void parseUrlParams(List<VariableElement> urlParams, RequestModel requestModel) {
        for (VariableElement varElement : urlParams) {
            UrlParam annotation = varElement.getAnnotation(UrlParam.class);
            String paramKey = annotation.value();
            requestModel.addField(ModelFieldDef.FieldType.UrlParam, paramKey, varElement.getSimpleName().toString(), varElement.asType());
        }
    }

    private void parseHeaderParams(List<VariableElement> headerParams, RequestModel requestModel) {
        for (VariableElement varElement : headerParams) {
            HeaderParam annotation = varElement.getAnnotation(HeaderParam.class);
            String paramKey = annotation.value();
            requestModel.addField(ModelFieldDef.FieldType.HeaderParam, paramKey, varElement.getSimpleName().toString(), varElement.asType());
        }
    }

    private void parseBody(VariableElement body, RequestModel requestModel) {
        if (body == null) return;
        String paramKey = "body";
        requestModel.addField(ModelFieldDef.FieldType.Body, paramKey, body.getSimpleName().toString(), body.asType());
    }
}

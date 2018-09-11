package cn.cloudwiz.dalian.commons.export.excel;

import org.springframework.expression.EvaluationContext;

import java.util.List;

public interface ContentParser {

	public List<ExcelCellData> parse(ContentConfig content, EvaluationContext context);
	
}

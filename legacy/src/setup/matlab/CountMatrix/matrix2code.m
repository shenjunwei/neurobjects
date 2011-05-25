function code = matrix2code (M)

num_rows = size (M,1);
row = [];
code=[];
for i=1:num_rows,
    values = strtrim(num2str(M(i,:),'%02d '));
    values = strrep(values,' ',',');
    code = sprintf ('%s\n{%s},',code,values);
end
code = sprintf ('%s\n{%s},',code,values);
return;
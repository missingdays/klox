import sys
from collections import namedtuple

Type = namedtuple('Type', 'name fields'.split())

def create_ast(baseclass, grammar_file):

	print("// THIS FILE IS AUTO GENERATED. SEE tool/generate_ast.py")
	
	print(f"abstract class {baseclass} " + "{")

	types = []

	with open(grammar_file) as f:
		for line in f:
			if line.startswith('#'):
				continue

			row = line.split(":")
			types.append(Type(name=row[0].strip(), fields=":".join(row[1:]).strip()))

	create_visitor(baseclass, types)

	for _type in types:
		create_type(name=_type.name, fields=_type.fields, baseclass=baseclass)

	print("  abstract fun<R> accept(visitor: Visitor<R>) : R?")

	print("}")

def create_type(name, fields, baseclass):
	print(f"  class {name} : {baseclass} " + "{") # class declaration

	for field in fields.split(','):                        # fields declaration
		print(f"    val {field.strip()}")

	print(f"    constructor({fields}) " + "{")                      # constructor
	for field in fields.split(','):
		field_name = field.split(":")[0].strip()
		print(f"      this.{field_name} = {field_name}")
	print('    }')

	print("")
	print("    override fun<R> accept(visitor: Visitor<R>) : R? { ")
	print(f"      return visitor.visit{name}{baseclass}(this)")
	print("    }")

	print('  }')
	print("")

def create_visitor(baseclass, types):
	print("  interface Visitor<R> {")
	for _type in types:
		print(f"    fun visit{_type.name}{baseclass} ({baseclass.lower()}: {_type.name}) : R?")
	print("  }")
	print("")

if __name__ == "__main__":
	argv = sys.argv

	if len(argv) != 3:
		print("Usage: generate_ast.py [baseclass] [ast types file]")

	baseclass = sys.argv[1]
	grammar_file = sys.argv[2]

	create_ast(baseclass, grammar_file)
